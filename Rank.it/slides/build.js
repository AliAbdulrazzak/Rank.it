// Generates Rank.it_Presentation.pptx in the project root.
const PptxGenJS = require("pptxgenjs");
const path = require("path");

const pptx = new PptxGenJS();
pptx.layout = "LAYOUT_WIDE"; // 13.33 x 7.5 in
pptx.title = "Rank.it";
pptx.author = "CSC 461 Team Rankers";
pptx.company = "West Chester University";

// Theme colors
const C = {
  bgDark: "0F172A",       // slate-900
  bgPanel: "1E293B",      // slate-800
  accent: "22C55E",       // green-500 (upvote)
  accent2: "EF4444",      // red-500  (downvote)
  text: "F8FAFC",         // slate-50
  muted: "94A3B8",        // slate-400
  brand: "38BDF8",        // sky-400
};

function addBackground(slide) {
  slide.background = { color: C.bgDark };
}

function addFooter(slide, pageNum) {
  slide.addText("Rank.it  ·  CSC 461 Group Project", {
    x: 0.4, y: 7.05, w: 8, h: 0.35,
    fontSize: 10, color: C.muted, fontFace: "Calibri",
  });
  slide.addText(String(pageNum), {
    x: 12.6, y: 7.05, w: 0.4, h: 0.35,
    fontSize: 10, color: C.muted, align: "right",
  });
}

function titleBar(slide, title, subtitle) {
  slide.addShape(pptx.ShapeType.rect, {
    x: 0.4, y: 0.4, w: 0.15, h: 0.9, fill: { color: C.brand }, line: { color: C.brand },
  });
  slide.addText(title, {
    x: 0.7, y: 0.35, w: 12, h: 0.7,
    fontSize: 32, bold: true, color: C.text, fontFace: "Calibri",
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.7, y: 1.0, w: 12, h: 0.5,
      fontSize: 16, color: C.muted, fontFace: "Calibri",
    });
  }
}

function bullets(slide, items, opts = {}) {
  const x = opts.x ?? 0.7;
  const y = opts.y ?? 1.7;
  const w = opts.w ?? 12;
  const h = opts.h ?? 5.0;
  slide.addText(
    items.map((t) => ({ text: t, options: { bullet: { type: "bullet" }, breakLine: true } })),
    {
      x, y, w, h,
      fontSize: opts.fontSize ?? 18, color: C.text, fontFace: "Calibri",
      paraSpaceAfter: 8, valign: "top",
    }
  );
}

let page = 0;
function newSlide() {
  const s = pptx.addSlide();
  addBackground(s);
  page++;
  return s;
}

// 1. Title slide
{
  const s = newSlide();
  s.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 13.33, h: 7.5, fill: { color: C.bgDark }, line: { color: C.bgDark },
  });
  s.addText("Rank.it", {
    x: 0, y: 2.4, w: 13.33, h: 1.4,
    fontSize: 96, bold: true, color: C.brand, align: "center", fontFace: "Calibri",
  });
  s.addText("Vote on what matters. Rank what wins.", {
    x: 0, y: 3.7, w: 13.33, h: 0.6,
    fontSize: 24, color: C.text, align: "center", fontFace: "Calibri",
  });
  s.addText("CSC 461 — Team Rankers", {
    x: 0, y: 4.5, w: 13.33, h: 0.5,
    fontSize: 18, color: C.muted, align: "center",
  });
  s.addText("Ali  ·  Ethan  ·  Jordan  ·  Eddie", {
    x: 0, y: 5.0, w: 13.33, h: 0.5,
    fontSize: 18, color: C.muted, align: "center",
  });
  addFooter(s, page);
}

// 2. What is Rank.it?
{
  const s = newSlide();
  titleBar(s, "What is Rank.it?", "An Android polling app for opinionated takes");
  bullets(s, [
    "Users browse polls organized by category (Food, Sports, Gaming, Movies, custom).",
    "Each poll has options that users upvote or downvote in real time.",
    "Votes are rate-limited (10 per 3-minute window) to keep rankings honest.",
    "All polls, options, and votes are persisted in Firebase Firestore.",
    "Users can create new polls, add new options to existing polls, and add new categories.",
    "A Settings screen offers dark mode, color themes, and adjustable font scale.",
  ]);
  addFooter(s, page);
}

// 3. Tech stack
{
  const s = newSlide();
  titleBar(s, "Tech Stack", "What we built it with");
  bullets(s, [
    "Language: Kotlin",
    "UI: Jetpack Compose with Material 3",
    "Architecture: MVVM with AndroidViewModel + StateFlow",
    "Backend: Firebase Firestore (real-time NoSQL) + Firebase Auth dependency",
    "Persistence: SharedPreferences for local vote-rate state and theme settings",
    "Build: Gradle (Kotlin DSL), Android compileSdk 36, minSdk 24, target 36",
    "IDE / tooling: Android Studio, Git + GitHub (WCU CooperLab org)",
  ]);
  addFooter(s, page);
}

// 4. Architecture
{
  const s = newSlide();
  titleBar(s, "Architecture", "One-way data flow");

  // Boxes
  const boxes = [
    { x: 0.7, y: 2.2, w: 2.6, h: 1.2, label: "Compose UI\n(PollScreen,\nSettingsScreen)" },
    { x: 4.0, y: 2.2, w: 2.6, h: 1.2, label: "PollViewModel\n(StateFlow)" },
    { x: 7.3, y: 2.2, w: 2.6, h: 1.2, label: "db (singleton)\nFirestore wrapper" },
    { x: 10.6, y: 2.2, w: 2.4, h: 1.2, label: "Firebase\nFirestore" },
  ];
  boxes.forEach((b) => {
    s.addShape(pptx.ShapeType.roundRect, {
      x: b.x, y: b.y, w: b.w, h: b.h,
      fill: { color: C.bgPanel }, line: { color: C.brand, width: 1.5 },
      rectRadius: 0.1,
    });
    s.addText(b.label, {
      x: b.x, y: b.y, w: b.w, h: b.h,
      fontSize: 14, color: C.text, align: "center", valign: "middle", bold: true,
    });
  });
  // Arrows
  for (let i = 0; i < boxes.length - 1; i++) {
    const from = boxes[i];
    const to = boxes[i + 1];
    s.addShape(pptx.ShapeType.line, {
      x: from.x + from.w, y: from.y + from.h / 2,
      w: to.x - (from.x + from.w), h: 0,
      line: { color: C.accent, width: 2, endArrowType: "triangle" },
    });
  }

  bullets(s, [
    "UI observes StateFlow<List<Poll>> via collectAsStateWithLifecycle — recomposes on change.",
    "ViewModel calls db.vote / db.addOption / db.createCategory; updates local state on success.",
    "db wraps every Firestore read/write and exposes simple callbacks back to the ViewModel.",
    "Local state and Firestore writes happen optimistically; reads on launch hydrate the UI.",
  ], { y: 4.0, h: 2.6, fontSize: 16 });
  addFooter(s, page);
}

// 5. Key features
{
  const s = newSlide();
  titleBar(s, "Key Features");
  bullets(s, [
    "Categories with chip-based filtering (All, Food, Sports, Gaming, Movies + user-added).",
    "Create polls with 2+ options; add new options to existing polls on the fly.",
    "Upvote / downvote with animated vote counts and percentage progress bars.",
    "Vote-budget rate limiter: 10 votes per 3 minutes, with a visible reset timer.",
    "Trending button to surface the most-voted polls.",
    "Settings: dark mode toggle, multiple color themes, adjustable font scale (80–150%).",
    "Cloud sync: every action writes to Firestore so polls are shared across devices.",
  ]);
  addFooter(s, page);
}

// 6. Data model
{
  const s = newSlide();
  titleBar(s, "Data Model", "Firestore collections");
  bullets(s, [
    "categories/{categoryId}        — one document per poll (name, filterCategory, createdAt)",
    "categories/{categoryId}/options/{optionId}   — subcollection: name, score, imageUrl",
    "filterCategories/{id}          — user-added category labels (name, createdAt)",
    "Local model mirrors this: Poll(id, firestoreId, title, category, options) +",
    "PollOption(id, firestoreId, name, votes).",
    "Vote = atomic FieldValue.increment on the option's score field.",
    "Vote-budget state lives in SharedPreferences (remaining + resetTime), not Firestore.",
  ], { fontSize: 16 });
  addFooter(s, page);
}

// 7. How we built it
{
  const s = newSlide();
  titleBar(s, "How We Built It", "Project timeline");
  bullets(s, [
    "Phase 1 — Proposal & scaffolding: agreed on Compose + Firestore; scaffolded Android project.",
    "Phase 2 — Core polling UI: data classes, ViewModel, list of polls with filter chips.",
    "Phase 3 — Voting logic: upvote/downvote, vote-budget rate limiter, score animations.",
    "Phase 4 — Firestore integration: db singleton, createCategory / addOption / vote / loadPolls.",
    "Phase 5 — Settings & theming: dark mode, color themes, font scale, persisted via prefs.",
    "Phase 6 — Polish: trending sort, percentage bars, animated counts, logo and branding.",
    "Phase 7 — Testing & release: closed testing build, bug fixes (e.g. invalid Firestore path).",
  ], { fontSize: 16 });
  addFooter(s, page);
}

// 8. Team roles
{
  const s = newSlide();
  titleBar(s, "Team Roles");

  const cards = [
    {
      name: "Ali",
      role: "UI & Voting Logic",
      bullets: [
        "Built Compose screens (PollScreen, PollCard, dialogs).",
        "Implemented upvote/downvote, animations, percentage bars.",
        "Wired ViewModel state and Firestore loadPolls integration.",
        "Owned trending feature and crash-bug fixes.",
      ],
    },
    {
      name: "Jordan",
      role: "Firestore / Database",
      bullets: [
        "Designed the Firestore schema (categories, options, filterCategories).",
        "Wrote db.kt: createCategory, addOption, vote, loadPolls, listen.",
        "Implemented score updates with FieldValue.increment.",
        "Improved voting reliability (read-after-write fixes).",
      ],
    },
    {
      name: "Ethan",
      role: "Theming & Settings",
      bullets: [
        "Built SettingsScreen (dark mode, color themes, font scale).",
        "Created the AppTheme palette set and Material 3 wiring.",
        "Designed the logo and overall visual identity.",
        "Managed Android keystore and closed-testing release.",
      ],
    },
    {
      name: "Eddie",
      role: "User Testing & QA",
      bullets: [
        "Drove playtests with classmates and external users.",
        "Logged bugs, edge cases, and confusing UX flows.",
        "Verified vote rate-limit and reset-timer behavior.",
        "Authored the Rank.It privacy policy page.",
      ],
    },
  ];

  const cw = 6.0, ch = 2.4;
  const positions = [
    { x: 0.5, y: 1.8 }, { x: 6.8, y: 1.8 },
    { x: 0.5, y: 4.4 }, { x: 6.8, y: 4.4 },
  ];

  cards.forEach((c, i) => {
    const p = positions[i];
    s.addShape(pptx.ShapeType.roundRect, {
      x: p.x, y: p.y, w: cw, h: ch,
      fill: { color: C.bgPanel }, line: { color: C.brand, width: 1.25 }, rectRadius: 0.1,
    });
    s.addText(c.name, {
      x: p.x + 0.2, y: p.y + 0.1, w: cw - 0.4, h: 0.45,
      fontSize: 22, bold: true, color: C.brand,
    });
    s.addText(c.role, {
      x: p.x + 0.2, y: p.y + 0.55, w: cw - 0.4, h: 0.35,
      fontSize: 14, italic: true, color: C.muted,
    });
    s.addText(
      c.bullets.map((t) => ({ text: t, options: { bullet: { type: "bullet" }, breakLine: true } })),
      {
        x: p.x + 0.25, y: p.y + 0.95, w: cw - 0.45, h: ch - 1.05,
        fontSize: 12, color: C.text, valign: "top", paraSpaceAfter: 4,
      }
    );
  });
  addFooter(s, page);
}

// 9. Challenges & lessons
{
  const s = newSlide();
  titleBar(s, "Challenges & Lessons");
  bullets(s, [
    "Optimistic UI vs Firestore IDs — newly-added options had empty firestoreId, so a fast vote built an odd-segment path and crashed. Fixed by writing the generated ID back into the local poll on add.",
    "Vote rate-limiting required local persistence so it survives app restarts.",
    "Compose recomposition + StateFlow took a few iterations to keep votes responsive without flicker.",
    "Coordinating on Firestore schema early made parallel work much easier.",
    "Real user testing (Eddie) caught edge cases that we missed in dev.",
  ]);
  addFooter(s, page);
}

// 10. Demo / closing
{
  const s = newSlide();
  titleBar(s, "Demo & Wrap-up");
  bullets(s, [
    "Live demo: create a poll, vote, watch counts animate, hit the rate limit.",
    "Closed testing build available via internal Play Console track.",
    "Repo: WCU-CS-CooperLab/csc461-s26-group-project-rankers",
    "Thanks for watching — questions?",
  ]);
  s.addText("Rank.it", {
    x: 0, y: 5.4, w: 13.33, h: 1.2,
    fontSize: 80, bold: true, color: C.brand, align: "center",
  });
  addFooter(s, page);
}

const outPath = path.resolve(__dirname, "..", "Rank.it_Presentation.pptx");
pptx.writeFile({ fileName: outPath }).then((f) => {
  console.log("WROTE:", f);
});
