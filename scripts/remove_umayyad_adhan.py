from pathlib import Path
import re
root = Path(__file__).resolve().parents[1]
for path in (root / 'feature/feature-prayer-times/src/main').glob('res/values*/strings.xml'):
    text = path.read_text(encoding='utf-8')
    text = re.sub(r'\s*<string name="bundled_adhan_umayyad_damascus">.*?</string>', '', text)
    path.write_text(text, encoding='utf-8', newline='')
raw = root / 'feature/feature-prayer-times/src/main/res/raw/adhan_umayyad_damascus.mp3'
if raw.exists(): raw.unlink()
print('Removed unverified Umayyad adhan asset and labels.')
