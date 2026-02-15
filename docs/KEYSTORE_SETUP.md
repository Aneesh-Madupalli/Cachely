# Keystore setup for signed release APK

Use this guide to create a signing keystore and add it to GitHub Secrets so CI can build a **signed release APK**.

---

## Step 1: Install Java (if needed)

You need `keytool` (comes with the JDK). Check:

```bash
keytool -help
```

If that works, skip to Step 2. If not, install a JDK (e.g. [Eclipse Temurin](https://adoptium.net/) or use Android Studio’s bundled JDK).

---

## Step 2: Create the keystore (one time)

Open a terminal in a **private folder** (e.g. your Desktop). **Do not** put the keystore inside the project or commit it.

### Windows (PowerShell)

```powershell
keytool -genkeypair -v -storetype PKCS12 -keystore cachely-release.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

### macOS / Linux

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore cachely-release.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

- **Prompts:** Enter a keystore password, then key password (you can use the same). Fill in name/organization or press Enter to skip.
- **Result:** A file `cachely-release.keystore` is created. **Back it up somewhere safe** (e.g. encrypted backup). If you lose it, you cannot update the app on the Play Store with the same key.

Remember:
- **Keystore password** (you’ll use this as `KEYSTORE_PASSWORD`)
- **Key password** (you’ll use this as `KEY_PASSWORD`)
- **Alias** is `upload` (you’ll use this as `KEY_ALIAS`)

---

## Step 3: Get the keystore as base64

CI expects the keystore as one long base64 string.

### Windows (PowerShell)

From the folder that contains `cachely-release.keystore`:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\cachely-release.keystore")) | Set-Clipboard
```

That copies the base64 string to the clipboard.  
To save it to a file instead (e.g. to paste into GitHub later):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\cachely-release.keystore")) | Out-File -Encoding ASCII keystore-base64.txt
```

Then open `keystore-base64.txt`, copy **the entire content** (one long line).

### macOS

```bash
base64 -i cachely-release.keystore -o keystore-base64.txt
```

Open `keystore-base64.txt` and copy the **entire** content.

### Linux

```bash
base64 -w 0 cachely-release.keystore > keystore-base64.txt
```

Open `keystore-base64.txt` and copy the **entire** content.

---

## Step 4: Add GitHub Secrets

1. On GitHub: open your **Cachely** repo → **Settings** → **Secrets and variables** → **Actions**.
2. Open the **Secrets** tab.
3. For each row below, click **New repository secret**, set **Name** and **Value**, then save.

| Secret name           | Value |
|-----------------------|--------|
| `KEYSTORE_BASE64`     | The **entire** base64 string from Step 3 (one long line, no spaces/line breaks). |
| `KEYSTORE_PASSWORD`   | The **keystore** password you set in Step 2. |
| `KEY_ALIAS`           | `upload` (or the alias you used in `keytool`). |
| `KEY_PASSWORD`        | The **key** password you set in Step 2. |

**Important for KEYSTORE_BASE64:** Paste the full base64 string. If you use the file, copy everything inside it (from the first character to the last). Do not add spaces or newlines.

---

## Step 5: Turn on signed release in CI

1. In the same repo: **Settings** → **Secrets and variables** → **Actions**.
2. Open the **Variables** tab.
3. **New repository variable**  
   - Name: `SIGNED_RELEASE`  
   - Value: `true`  
4. Save.

---

## Step 6: Run the workflow

Push a commit or run the **Build APK** workflow manually (Actions → Build APK → Run workflow). The workflow will:

- Decode `KEYSTORE_BASE64` to a keystore file
- Build a **signed release APK**
- Upload it as an artifact and (if configured) send it to Telegram

---

## Troubleshooting

- **“Unrecognized named-value: 'secrets'”**  
  You must use the **variable** `SIGNED_RELEASE` in the workflow, not `secrets` in `if` conditions. The workflow in this repo is already set up for that.

- **Signing fails in CI**  
  Check that all four secrets are set and that `KEYSTORE_BASE64` is the **complete** base64 string (no truncation, no extra newlines).

- **Forgot password or alias**  
  You cannot recover them. You’ll need to create a new keystore and use it for new builds (existing installs may need to be uninstalled before installing an APK signed with the new key).
