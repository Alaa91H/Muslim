# -*- coding: utf-8 -*-
import io

p = "scripts/full_format_audit.py"
s = io.open(p, encoding="utf-8").read()

old = '                        is_str = bool(re.search(r"\\.toString\\(\\)", a)) or re.search(r\'"[^"]*"\', a)\n'
old += '                        is_int = bool(re.search(r"\\.count\\b|\\.size\\b|\\.number\\b|\\.value\\b|\\.length\\b|\\b\\d+\\b", a)) and not re.search(r\'"[^"]*"\', a) and ".toString()" not in a'
new = '                        is_int = bool(re.search(r"\\.count\\b|\\.size\\b|\\.number\\b|\\.value\\b|\\.length\\b|\\b\\d+\\b", a)) and ".toString()" not in a\n'
new += '                        is_str = (re.search(r"\\.toString\\(\\)", a) or re.search(r\'"[^"]*"\', a)) and not is_int'
assert s.count(old) == 1, s.count(old)
s = s.replace(old, new)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("PATCHED")
