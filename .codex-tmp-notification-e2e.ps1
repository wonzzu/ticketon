$ErrorActionPreference = 'Stop'

$login = Invoke-RestMethod `
    -Method Post `
    -Uri 'http://localhost:8080/auth/login' `
    -ContentType 'application/json' `
    -Body '{"email":"normal@test.com","password":"test1234"}' `
    -SessionVariable webSession

$token = $login.data.accessToken
$headers = @{ Authorization = "Bearer $token" }
$before = Invoke-RestMethod `
    -Method Get `
    -Uri 'http://localhost:8080/notifications/unread-count' `
    -Headers $headers `
    -WebSession $webSession

$messageId = [guid]::NewGuid().ToString()
$aggregateId = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$occurredAt = (Get-Date).ToString('yyyy-MM-ddTHH:mm:ss')
$payload = '{"paymentId":3000011,"reservationId":3000008,"memberId":4,"canceledAmount":110000,"canceledAt":"' + $occurredAt + '","sellerId":2,"performanceEventId":1,"settlementDate":"2026-09-24","paidDate":"2026-09-23"}'
$payloadHex = [Convert]::ToHexString([Text.Encoding]::UTF8.GetBytes($payload))
$sql = "INSERT INTO outbox_event (aggregate_id, created_at, updated_at, message_id, aggregate_type, payload, event_type, status, event_version, event_sequence) VALUES ($aggregateId, NOW(6), NOW(6), '$messageId', 'PAYMENT', CONVERT(0x$payloadHex USING utf8mb4), 'PAYMENT_CANCELED', 'PENDING', 1, 1);"
$remoteCommand = "docker exec ticketon-mysql mysql -uroot -pqwer1234 -D ticketing -e `"$sql`""

$sseFile = Join-Path (Get-Location) '.codex-tmp-sse-output.txt'
if (Test-Path $sseFile) {
    Remove-Item -LiteralPath $sseFile -Force
}

$curlArguments = "-sN -H `"Authorization: Bearer $token`" http://localhost:8080/notifications/stream"
$curl = Start-Process `
    -FilePath 'curl.exe' `
    -ArgumentList $curlArguments `
    -RedirectStandardOutput $sseFile `
    -WindowStyle Hidden `
    -PassThru

try {
    Start-Sleep -Seconds 2
    & 'C:\Windows\System32\OpenSSH\ssh.exe' 'wonzz@100.72.172.89' $remoteCommand | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw '테스트 Outbox 이벤트 저장에 실패했습니다.'
    }

    $received = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Milliseconds 500
        if ((Test-Path $sseFile) -and ((Get-Content $sseFile -Raw) -match 'event:notification')) {
            $received = $true
            break
        }
    }

    $after = Invoke-RestMethod `
        -Method Get `
        -Uri 'http://localhost:8080/notifications/unread-count' `
        -Headers $headers `
        -WebSession $webSession

    $list = Invoke-RestMethod `
        -Method Get `
        -Uri 'http://localhost:8080/notifications?size=20' `
        -Headers $headers `
        -WebSession $webSession

    $created = $list.data.items | Where-Object {
        $_.referenceId -eq 3000008 -and $_.type -eq 'PAYMENT_CANCELED'
    } | Sort-Object id -Descending | Select-Object -First 1

    [pscustomobject]@{
        MessageId = $messageId
        SseReceived = $received
        UnreadBefore = $before.data.count
        UnreadAfter = $after.data.count
        NotificationId = $created.id
        NotificationTitle = $created.title
        NotificationContent = $created.content
        SsePayload = (Get-Content $sseFile -Raw)
    } | ConvertTo-Json -Depth 5
}
finally {
    if (!$curl.HasExited) {
        Stop-Process -Id $curl.Id -Force
    }
    if (Test-Path $sseFile) {
        Remove-Item -LiteralPath $sseFile -Force
    }
}
