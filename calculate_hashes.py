import os
import hashlib

folder = "evidence_demo"
files = [
    "01_FIR_245_2026.pdf",
    "02_Scene_and_DVR_Seizure_Memo.pdf",
    "03_CCTV_Frame_ORIGINAL_2230.png",
    "04_CCTV_Frame_EDITED_BY_DEFENSE_2130.png",
    "05_Phone_Forensic_Report_FSL_2026_0612.pdf"
]

hashes = {}
output_lines = []

for fname in files:
    fpath = os.path.join(folder, fname)
    with open(fpath, "rb") as f:
        data = f.read()
        sha256 = hashlib.sha256(data).hexdigest()
        hashes[fname] = sha256
        output_lines.append(f"{fname}: {sha256} ({len(data)} bytes)")

hashes_txt_path = os.path.join(folder, "HASHES.txt")
with open(hashes_txt_path, "w") as out:
    out.write("\n".join(output_lines) + "\n")

print("=== SHA-256 CALCULATIONS COMPLETED ===")
for line in output_lines:
    print(line)

h3 = hashes["03_CCTV_Frame_ORIGINAL_2230.png"]
h4 = hashes["04_CCTV_Frame_EDITED_BY_DEFENSE_2130.png"]

print("\n=== HASH COMPARISON: FILE 3 vs FILE 4 ===")
print(f"File 3 (Original 22:30) Hash: {h3}")
print(f"File 3 First 8 Chars:         {h3[:8]}")
print(f"File 4 (Defense 21:30)  Hash: {h4}")
print(f"File 4 First 8 Chars:         {h4[:8]}")
print(f"Hashes are DIFFERENT:         {h3 != h4}")
