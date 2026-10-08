const fs = require('fs');
const path = require('path');

const BASE_DIR = path.resolve("c:/Users/midor/Documents/reto");

// Function to clean emojis from text
function removeEmojis(text) {
  return text.replace(/[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F700}-\u{1F77F}\u{1F780}-\u{1F7FF}\u{1F800}-\u{1F8FF}\u{1F900}-\u{1F9FF}\u{1FA00}-\u{1FA6F}\u{1FA70}-\u{1FAFF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{2300}-\u{23FF}\u{2B50}\u{200D}\u{FE0F}]/gu, '').replace(/  +/g, ' ');
}

// 1. Clean all MODRINTH_METADATA.md files
const folders = fs.readdirSync(BASE_DIR).filter(f => f.startsWith("nexoria-"));
folders.forEach(folder => {
  const metaPath = path.join(BASE_DIR, folder, "MODRINTH_METADATA.md");
  if (fs.existsSync(metaPath)) {
    let content = fs.readFileSync(metaPath, "utf8");
    content = removeEmojis(content);
    fs.writeFileSync(metaPath, content, "utf8");
  }
});

// 2. Clean BIO and Catalog
const bioPath = path.join(BASE_DIR, "MODRINTH_PROFILE_BIO.md");
if (fs.existsSync(bioPath)) {
  let content = fs.readFileSync(bioPath, "utf8");
  content = removeEmojis(content);
  fs.writeFileSync(bioPath, content, "utf8");
}

const catPath = path.join(BASE_DIR, "MODRINTH_MASTER_CATALOG.md");
if (fs.existsSync(catPath)) {
  let content = fs.readFileSync(catPath, "utf8");
  content = removeEmojis(content);
  fs.writeFileSync(catPath, content, "utf8");
}

console.log("All emojis removed successfully from all metadata files and documentations!");
