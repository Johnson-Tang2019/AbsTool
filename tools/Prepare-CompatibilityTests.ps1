param([switch]$PrepareServuxServer, [switch]$AcceptMinecraftEula)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$testMods = Join-Path $projectRoot 'build/reference/testmods'
[IO.Directory]::CreateDirectory($testMods) | Out-Null
$versions = [ordered]@{
    'servux.jar' = 'cjAxEbhD'
    'jade.jar' = 'ue8CO97w'
    'iris.jar' = 'gxZWWnKH'
    'sodium.jar' = 'xJZxADzI'
    'ComplementaryReimagined.zip' = 'ErCjThzb'
}
foreach ($entry in $versions.GetEnumerator()) {
    $release = Invoke-RestMethod "https://api.modrinth.com/v2/version/$($entry.Value)"
    $artifact = @($release.files | Where-Object primary)[0]
    $destination = Join-Path $testMods $entry.Key
    if (!(Test-Path -LiteralPath $destination) -or
            (Get-FileHash -Algorithm SHA512 -LiteralPath $destination).Hash -ne $artifact.hashes.sha512) {
        Invoke-WebRequest $artifact.url -OutFile $destination
    }
    if ((Get-FileHash -Algorithm SHA512 -LiteralPath $destination).Hash -ne $artifact.hashes.sha512) {
        throw "Checksum mismatch: $($entry.Key)"
    }
    Write-Output "Verified $($entry.Key): $($release.version_number)"
}
if ($PrepareServuxServer) {
    if (!$AcceptMinecraftEula) { throw 'Pass -AcceptMinecraftEula only after accepting https://aka.ms/MinecraftEULA.' }
    $serverRoot = Join-Path $projectRoot '26.2Fabric/build/run/servuxServer'
    [IO.Directory]::CreateDirectory($serverRoot) | Out-Null
    [IO.File]::WriteAllText((Join-Path $serverRoot 'eula.txt'), "eula=true`n")
    $properties = @'
server-ip=127.0.0.1
server-port=25576
online-mode=false
enforce-secure-profile=false
level-type=minecraft:flat
view-distance=12
simulation-distance=5
max-players=2
spawn-protection=0
'@
    $ops = '[{"uuid":"380df991-f603-344c-a090-369bad2a924a","name":"Dev","level":4,"bypassesPlayerLimit":true}]'
    foreach ($file in @(@('server.properties', $properties), @('ops.json', $ops))) {
        $destination = Join-Path $serverRoot $file[0]
        if (!(Test-Path -LiteralPath $destination)) { [IO.File]::WriteAllText($destination, $file[1]) }
    }
    Write-Output "Prepared local test server: $serverRoot"
}
