# Auto-build and jpackage script
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\build_jpackage.ps1
# This script:
#  - finds all .java files under current folder (recursively, including src/)
#  - compiles them into classes/ (preserving packages)
#  - detects the Java file that contains public static void main and computes its FQN
#  - creates an executable jar and runs jpackage to produce an app-image in dist/

$JarName = "homeworkapp.jar"
$AppName = "HomeworkApp"
$AppVersion = "1.0"
$OutDir = "dist"
$PackageInput = "package_input"
$ClassesDir = "classes"

# Check javac and jpackage
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Error "javac not found. Please install a JDK and ensure javac is in PATH."
    exit 1
}
if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "jpackage not found. Please install a JDK (17+) that includes jpackage and ensure jpackage is in PATH."
    exit 1
}

# find .java files
$javaFiles = Get-ChildItem -Path . -Recurse -Include *.java -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
if (-not $javaFiles) {
    Write-Error "No .java files found under current folder. Please run this script from your project root."
    exit 2
}

# prepare classes dir
if (Test-Path $ClassesDir) { Remove-Item $ClassesDir -Recurse -Force }
New-Item -ItemType Directory -Path $ClassesDir | Out-Null

# compile
Write-Host "Compiling" ($javaFiles.Count) "java files..."
# build argument list for javac
$argList = @("-d", $ClassesDir) + $javaFiles
& javac @argList
if ($LASTEXITCODE -ne 0) {
    Write-Error "javac failed"
    exit 3
}

# detect main class
$mainMatch = Select-String -Path $javaFiles -Pattern "public\s+static\s+void\s+main" -List | Select-Object -First 1
if (-not $mainMatch) {
    Write-Warning "No main() method detected. Creating jar without main-class; jpackage may fail unless you provide --main-class."
    $mainClass = ""
} else {
    $mainPath = $mainMatch.Path
    $content = Get-Content $mainPath -Raw
    $pkgMatch = [regex]::Match($content, "^\s*package\s+([a-zA-Z0-9_.]+)\s*;", "Multiline")
    if ($pkgMatch.Success) { $pkg = $pkgMatch.Groups[1].Value } else { $pkg = "" }
    $className = [System.IO.Path]::GetFileNameWithoutExtension($mainPath)
    if ($pkg -ne "") { $mainClass = "$pkg.$className" } else { $mainClass = $className }
    Write-Host "Detected main class: $mainClass"
}

# create jar
if (Test-Path $JarName) { Remove-Item $JarName -Force }
if ($mainClass -ne "") {
    Write-Host "Creating runnable jar with Main-Class $mainClass..."
    & jar cfe $JarName $mainClass -C $ClassesDir .
} else {
    Write-Host "Creating jar without main-class..."
    & jar cf $JarName -C $ClassesDir .
}
if ($LASTEXITCODE -ne 0) { Write-Error "jar creation failed"; exit 4 }

# prepare package_input
if (Test-Path $PackageInput) { Remove-Item $PackageInput -Recurse -Force }
New-Item -ItemType Directory -Path $PackageInput | Out-Null
Copy-Item $JarName -Destination $PackageInput -Force
if (Test-Path "data") {
    Copy-Item -Recurse -Path "data" -Destination $PackageInput
    Write-Host "Included data/ directory."
} else {
    Write-Host "No data/ directory found; continuing without it."
}

# run jpackage
Write-Host "Running jpackage to build app-image..."
$jpackageArgs = @(
    "--input", $PackageInput,
    "--name", $AppName,
    "--main-jar", $JarName,
    "--main-class", $mainClass,
    "--type", "app-image",
    "--dest", $OutDir,
    "--app-version", $AppVersion
)
# if mainClass is empty, remove the --main-class parameter
if ($mainClass -eq "") {
    $jpackageArgs = $jpackageArgs | Where-Object { $_ -ne "--main-class" -and $_ -ne $mainClass }
}

$jpackageCmd = "jpackage " + ($jpackageArgs -join " ")
Write-Host
