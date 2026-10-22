# 🎬 EvidLock Live Courtroom Demo Script
### Case Study: *"The 10:30 PM Gate Camera"* (FIR No. 245/2026)
*All names, locations, and events are strictly fictional demo exhibits.*

---

## 📖 The 4-Sentence Case Story

1. On 14 October 2026, 23-year-old Priya Demo was found dead in her flat at Sunrise Apartments, Pune, with the door unlocked.
2. The society's perimeter gate camera recorded suspect Karan Demo walking through the entrance at **22:30 PM**, and police seized the DVR the next morning.
3. In court, the defense lawyer dramatically presents an alternative video file showing Karan entering at **21:30 PM**, claiming an alibi and accusing the police of altering the video to frame him.
4. The prosecution opens **EvidLock**, verifies the SHA-256 cryptographic seal placed on Day 1, and instantly proves mathematically that the defense file is tampered while the police evidence has remained 100% untouched.

---

## 🏛️ The 3-Step Presentation Walkthrough for Judges

### Step 1: The Defense Twist (Show the Two Images Side-by-Side)
* Open `03_CCTV_Frame_ORIGINAL_2230.png` and `04_CCTV_Frame_EDITED_BY_DEFENSE_2130.png`.
* **Say to Sir/Judges**:
  > *"Sir, look at these two CCTV screenshots. Visually, every single pixel, shadow, gate bar, and person silhouette is identical — **except one character in the timestamp** (`22:30:12` vs `21:30:12`). To the naked eye in a normal courtroom, a judge cannot tell which one was modified."*

---

### Step 2: The Hash Verification (Run SHA-256 Proof)
* Open `HASHES.txt` or run:
  ```powershell
  python calculate_hashes.py
  ```
* Show the side-by-side hash comparison:
  * **Original Police CCTV Frame (22:30)**:
    `70a15a9dc5b725358a91a372a1d2ad198e52ba6c793f53b69e437a848b398acd` *(Starts with `70a15a9d`)*
  * **Defense Altered Frame (21:30)**:
    `0e00ea970bc06bc18f391002cee49b316e2af6725815125a2a9a1087f114d217` *(Starts with `0e00ea97`)*
* **Say to Sir/Judges**:
  > *"Because of SHA-256 cryptographic properties, changing even 1 second in a video completely changes the entire 64-character hash digest. The defense file has a completely different hash: `0e00ea97...`."*

---

### Step 3: Open EvidLock & Produce Incontestable Proof
1. Open the **EvidLock Evidence Vault** at [http://localhost:4200/documenti](http://localhost:4200/documenti) (or your live URL).
2. Locate Evidence Tag **`EV-2026-00245-03`** (*Original CCTV Video Frame - CAM 01 Gate*).
3. Point to the Hash Column:
   * It shows `70a15a9d...` sealed on **15-Oct-2026 at 11:00 AM** — the morning after the incident!
4. Click the **Ledger** button:
   * Show **Block 0 (Genesis)**: Intake hash computed on Day 1 by Investigator Priya Sharma.
   * Show **Block 1 (Transfer)**: Dual-custody handover to FSL Forensic Director Dr. Vikram Desai.
   * Click **Verify Chain**: Green badge displays **"Chain 100% Intact & Verified"**.
5. Click the **Sec. 65B / PDF** button:
   * Download the official **Section 65B Indian Evidence Act & Section 63 BSA 2023 Certificate**.
* **Say to Sir/Judges**:
  > *"Sir, EvidLock proves that the police sealed `70a15a9d...` within hours of the crime, before the suspect was even arrested. Because our hash chain is append-only and enforced by PostgreSQL triggers, no police officer or hacker could have backdated or edited this timestamp. The defense copy is exposed as an altered forgery, and the original evidence is fully protected!"*

---

## 📁 Demo Evidence Files Summary

| File Name | Size | SHA-256 Digest (First 8 Chars) | Role in Demo |
| :--- | :--- | :--- | :--- |
| `01_FIR_245_2026.pdf` | 3.7 KB | `e5f57a7e` | Case initiation document (Demo PS, Pune). |
| `02_Scene_and_DVR_Seizure_Memo.pdf` | 3.8 KB | `c0400753` | Panchnama of physical DVR seizure with 2 witnesses (no hash written). |
| `03_CCTV_Frame_ORIGINAL_2230.png` | 23.5 KB | **`70a15a9d`** | **Genuine evidence sealed into EvidLock vault**. |
| `04_CCTV_Frame_EDITED_BY_DEFENSE_2130.png` | 23.5 KB | **`0e00ea97`** | **The defense forgery (different hash!)**. |
| `05_Phone_Forensic_Report_FSL_2026_0612.pdf` | 4.1 KB | `185bb915` | SFL Lab Report: Priya's WhatsApp message at 23:30. |

---

## 🗄️ Database Setup (Neon PostgreSQL)
To see this case live in your EvidLock dashboard, simply copy and paste the contents of `neon_insert_case.sql` into your **Neon Console ➔ SQL Editor** and click **Run**.
