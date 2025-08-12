param (
    [string]$Path
)

if (-not $Path) {
    $Path = Get-Location
}

# Отримуємо всі файли в поточній папці та підпапках
Get-ChildItem -Path $Path -Recurse -File |  
    Get-Content | 
    Set-Clipboard

Write-Host "Вміст усіх файлів у папці '$Path' та її підпапках скопійовано в буфер обміну."
