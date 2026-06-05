# ARISE User Journey & Screen Architecture Blueprint

This document defines the complete UX blueprint and architectural design for **ARISE: Life OS Alarm**, a 100% offline-first, private personal manager.

---

## 1. DYNAMIC COLOR SYSTEM & DESIGN PATTERNS
We adopt the **"Cosmic Slate"** design system defined in `FRONTEND_GUIDELINES.md`:
*   **Fonts**: *Orbitron* (Time display, typography headings, tracking), *Inter* (body, labels, instructional copy), and *Share Tech Mono* (data displays, statistics, counts).
*   **Colors**: Primary branding is driven by `#00F5FF` (Brand Cyan: active alarms, glows), `#7C3AED` (Brand Violet: AI insight indicators, achievements), and `#FF006E` (Brand Pink: snooze actions, alarm triggers). Backgrounds leverage a deep `#04070F` base layer with `#080F1E` surface cards and subtle `#0D1529` inputs.
*   **Haptics**: Responsive micro-vibrations are triggered for all toggle switch items and selector adjustments.

---

## 2. NAVIGATION MAP & COMMAND PATHS

```
[Onboarding]
     │
     ▼
[Home Dashboard] (Command Center) ◄──────────────┐ (Top / Bottom Bar Navigation)
     ├──► [Alarm List] ──► [Alarm Creation / Edit]│
     ├──► [Calendar Month/Week/Day View] ──► [Event Creation]
     ├──► [Goal Dashboard] ──► [Goal Details]
     ├──► [Habit Tracker]
     ├──► [Daily Mission Screen]
     ├──► [Sleep Dashboard]
     ├──► [Statistics Dashboard] ──► [Timeline View]
     └──► [Settings] ──► [Security (PIN)] / [Backup & Restore]
```

### Navigation Keys
*   **Global Bottom Navigation Bar**: Swaps between:
    1.  **Home** (`home_tab` - Command Center with quick cards)
    2.  **Alarms** (`alarms_tab` - Alarm management list)
    3.  **Calendar** (`calendar_tab` - Grid and day events list)
    4.  **Habits/Goals** (`growth_tab` - Streaks, habits, goals)
    5.  **Metrics** (`metrics_tab` - Sleep scores and long-term stats history)
*   **Global App Bar (Action Panel)**: Standard top bar containing a profile avatar/skin selector on the left, a digital live-pulse clock in Orbitron in the center, and a Settings wheel with a Security status indicator on the right.

---

## 3. COMPREHENSIVE SCREEN ARCHITECTURE SPECIFICATIONS

### Screen 1: Onboarding
1.  **Screen Purpose**: Introduce the "Active Life Architect" concept, configure local setup, choose the UI theme/skin, and initialize the database.
2.  **Layout Structure**: Single-view vertical swiper (3-stage carousel) with large negative space and a persistent progress indicator.
3.  **Components**:
    *   *Hero Animation Canvas*: Interactive, slow-pulsing radial gradient vector representing a circadian rhythm.
    *   *Headline Display*: Orbitron typography paired with bold minimalist text descriptors.
    *   *Theme Skin Selectors*: Tap-targeted horizontal cards demonstrating "Deep Space", "Cyber Neon Check", and "Aurora Dawn Light".
    *   *Action Button*: Large primary gradient button (`#00F5FF` to `#7C3AED`) with text "ARISE ARCHITECT".
4.  **User Interactions**:
    *   Swipe or tap "Next" to scroll pages.
    *   Tapping theme cards triggers brief haptic pulse, instantly swapping localized screen state previews.
5.  **Navigation Flow**: Slides to the next stage; on final card press, writes `first_boot_completed` to `AppSetting` list, and navigates directly to the *Home Dashboard*.
6.  **Empty States**: N/A.
7.  **Error States**: Form validation displays an inline red `#FF3D00` text if a name input is left empty.
8.  **Accessibility Considerations**: Reading indicators are labeled as page fractions ("1 of 3"). All buttons are forced to a minimum height of 48.dp.

### Screen 2: Home Dashboard (The Command Center)
1.  **Screen Purpose**: Serve as the user's ultimate single-screen life dashboard, aggregating priority actions, timelines, and growth stats in one beautiful unified card block.
2.  **Layout Structure**: Vertical scroll layout with asymmetric side-pinned stats. Screen horizontal padding is exactly 20.dp.
3.  **Components**:
    *   *Hero Clock Header*: Large display text in Orbitron (`type.alarm.xl`) showing the current hour and next active alarm timestamp underneath.
    *   *Sleep Score Circular Gauge*: A high-contrast neon-cyan ring gauge indicating yesterday's sleep rating (e.g. `84%`).
    *   *Next Alarm Snippet*: Small card with quick toggle and time, showing a pink glow background if scheduled within the next 4 hours.
    *   *Today's Calendar Agenda*: Small horizontal timeline row showing today's primary meetings/events.
    *   *Top 3 Daily Missions*: A custom checklists section showing today's highest priority goals/habits.
    *   *Growth Indicators*: Tiny counter tags displaying active habit streak counts.
4.  **User Interactions**:
    *   Tapping the large clock scrolls directly to *Alarm List*.
    *   Tapping the Sleep Circle navigates to *Sleep Dashboard*.
    *   Quick checking a mission updates database status and triggers visual screen confetti.
5.  **Navigation Flow**: Standard bottom bar pathways, and direct deep-links from widget taps.
6.  **Empty States**: Shows decorative text "All clean! Sleep soundly tonight" if no alarms/missions are set.
7.  **Error States**: Fallback to safe local cached data if an asynchronous operation misses updates.
8.  **Accessibility Considerations**: Screen elements are grouped sequentially to allow smooth swipe-through for screen readers.

### Screen 3: Alarm List
1.  **Screen Purpose**: Comprehensive overview of all user alarms, sorted chronologically with easy-to-read active toggles.
2.  **Layout Structure**: Vertical scrollable list of custom cards, utilizing structural borders.
3.  **Components**:
    *   *Clock Item Cards*: Large Orbitron display times (`type.alarm.lg`) with categorical text tags ("Work", "Health").
    *   *Quick Switch Toggles*: Sliding toggle buttons showing vibrant cyan track color when enabled.
    *   *Day Repeat Pips*: Small circular chips representing days of the week, with active days colored in `#00F5FF`.
    *   *Floating Action Button*: Circular primary gradient FAB positioned in the bottom-right corner.
4.  **User Interactions**:
    *   Toggle alarm on/off (triggers ripple and crisp haptic click).
    *   Short press Alarm Card to view *Alarm Edit*.
    *   Long press Alarm Card to open a deletion confirmation popup.
5.  **Navigation Flow**: Bottom bar or back navigation. FAB opens *Alarm Creation*.
6.  **Empty States**: Centered hand-crafted vector clock representation with friendly instructional copy: "No active alarms. Tap '+' to build your first wake rhythm".
7.  **Error States**: Sticky red alert bar is shown if an unexpected background alarm scheduling failure occurs.
8.  **Accessibility Considerations**: All item buttons contain full content descriptions (e.g., "7:00 AM Work Alarm, active Mondays and Wednesdays").

### Screen 4: Alarm Creation
1.  **Screen Purpose**: Allow precise custom scheduling of a new wake-up trigger, choosing difficulties, active sounds, and connected event hooks.
2.  **Layout Structure**: Two-column scrolling layout, separating primary time selection from advanced options.
3.  **Components**:
    *   *Scroll Time Pickers*: Distinct adjacent spinning wheels for hours, minutes, and AM/PM.
    *   *Challenge Selectors*: Interactive grid buttons for selecting "None", "Math", "Memory", "Shake", or "Rhythm".
    *   *Difficulty Chips*: Colored segment control indicating "Easy" (green), "Medium" (orange), "Hard" (pink).
    *   *Volume & Gradual Controls*: Gradual volume crescendo slider alongside horizontal vibration style selectors.
4.  **User Interactions**:
    *   Scroll picker circles to choose standard time intervals.
    *   Tap challenge options to reveal secondary challenge parameters.
    *   Drag dynamic speed/gradual slider curves.
5.  **Navigation Flow**: Back arrow returns to *Alarm List* without saving. Pressing secondary flat button labeled "SAVE PATH" commits to database and pops up success toast.
6.  **Empty States**: N/A.
7.  **Error States**: Confirms valid scheduling coordinates; prints a red border over selected days if no weekday is highlighted.
8.  **Accessibility Considerations**: Screen reader speaks hours and custom sliders clearly as percentage integers.

### Screen 5: Alarm Edit
1.  **Screen Purpose**: Modify an existing scheduled alarm's properties, challenges, or connected schedules.
2.  **Layout Structure**: Matches *Alarm Creation* layout structure to prevent mental friction, adding distinct delete indicators.
3.  **Components**:
    *   *Preset Value Population*: Form components initialized directly with loaded entity values.
    *   *Delete Alarm Action Button*: Outlined red-pink action button (`color.brand.pink`) centered on the screen base.
    *   *Link Event Grid Selector*: Dynamic drop-down showing calendar event connections.
4.  **User Interactions**:
    *   Direct adjustment of time picker wheel coordinates.
    *   Tapping "Delete Alarm" triggers structural dialog layer.
5.  **Navigation Flow**: Pops backwards instantly to *Alarm List* upon successful execution.
6.  **Empty States**: N/A.
7.  **Error States**: Form validator prevents saving duplicate alarm settings for identical times on identical days.
8.  **Accessibility Considerations**: Interactive item descriptions clarify existing parameters clearly before changes occur.

### Screen 6: Alarm Challenge Screen
1.  **Screen Purpose**: Ensure the user is physically/mentally conscious before allowing the active alarm audio signal to terminate.
2.  **Layout Structure**: Full-screen, high-contrast overlay. Large display headers alongside central interactive components.
3.  **Components**:
    *   *Ringing Header Title*: Pulsating, flashing alarm bell icons and Orbitron digital clocks glowing in deep pink and cyan hues.
    *   *Challenge Core Interactors*:
        *   *Math Challenge*: Multi-step math equations with custom digital keypads.
        *   *Memory Challenge*: Interactive 3x3 grids of flashing geometric pips.
        *   *Shake Challenge*: Smooth circular progress bar tracking active dynamic accelerations.
    *   *Emergency Snooze Lever*: Sliding gesture item labeled "SLIDE TO SNOOZE (9 MIN)" wrapped in transparent layers.
4.  **User Interactions**:
    *   Type-in correct equation solutions.
    *   Successive taps on flashing grids.
    *   Energetically shake the mobile phone.
5.  **Navigation Flow**: Stays anchored, blocking home key bypass checks, until the criteria are resolved, popping database logs and navigating to *Home Dashboard*.
6.  **Empty States**: N/A.
7.  **Error States**: Invalid equation inputs flash a crimson border and emit a short, low audio warning note.
8.  **Accessibility Considerations**: Shake challenge features a distinct "Tap Challenge" fallback mode toggleable on screen for restricted mobility.

### Screen 7: Calendar Day View
1.  **Screen Purpose**: Chronological timeline of events, tasks, and alarm cues scheduled for a single explicit date.
2.  **Layout Structure**: Timetable list (24-hour strip) alongside a persistent side scrolling event bar.
3.  **Components**:
    *   *Day Header Strip*: High-contrast daily calendar date with days adjacent.
    *   *Hour Slot Cards*: Floating event list, color-accented by category profiles.
    *   *Wake/Goal Track Blocks*: Integrated blocks marking sleeping boundaries and completed habits.
4.  **User Interactions**:
    *   Swiping horizontally slides adjacent days smoothly into view.
    *   Tapping an empty hour coordinates slot initiates *Event Creation*.
5.  **Navigation Flow**: Swaps between month/week tab coordinates directly.
6.  **Empty States**: Simple digital illustration showing an empty chair with caption: "No events today. Use your free time to construct new daily habits".
7.  **Error States**: Visual alert if a background database sync indicator shows outdated local clock tags.
8.  **Accessibility Considerations**: Screen readers speak chronological intervals and schedule hours continuously.

### Screen 8: Calendar Week View
1.  **Screen Purpose**: Micro overview of the active week, balancing tasks across working intervals.
2.  **Layout Structure**: Horizontal 7-column block layout, optimizing visual space.
3.  **Components**:
    *   *Weekly Grid Columns*: Clean day partitions with colored vertical accent lines outlining commitments.
    *   *Task Load Bars*: Compact visual color-intensity meters showing how congested a specific calendar day is.
4.  **User Interactions**:
    *   Tapping a specific day card transitions focus directly to *Calendar Day View*.
    *   Horizontal drag sweeps through previous or future work weeks.
5.  **Navigation Flow**: Simple fluid cross-screen back and forth.
6.  **Empty States**: Displays clean grid indicators with "Build simple reminders to maintain a healthy weekly rhythm".
7.  **Error States**: Standard localized warning messages.
8.  **Accessibility Considerations**: Screen reader scans day summaries efficiently ("Monday, 2 items scheduled").

### Screen 9: Calendar Month View
1.  **Screen Purpose**: Macro schedule tracking, showing daily density, appointments, and upcoming wake patterns.
2.  **Layout Structure**: Classic 7-column month grid, taking up 85% of standard phone screen space.
3.  **Components**:
    *   *Cell Indicators*: Grid blocks representing days containing subtle color dots to communicate events, habits, and tasks.
    *   *Selected Day Agenda*: Compact bottom panel that expands to show item lists for the highlighted day.
4.  **User Interactions**:
    *   Tap a specific matrix cell to expand and list the corresponding daily schedules below.
    *   Swipe vertically or horizontally to slide between months.
5.  **Navigation Flow**: Selecting bottom panel items takes user directly into details.
6.  **Empty States**: Cellular dots are empty, showing: "Ready for blueprints. Tap on a calendar day to schedule events".
7.  **Error States**: Displays safe structural fallbacks if initial system clocks cannot be fetched.
8.  **Accessibility Considerations**: Matrix tables are fully navigable via standard accessibility direction buttons.

### Screen 10: Event Creation
1.  **Screen Purpose**: Add a new calendar appointment and automatically configure prep or commute alarm coordinates if desired.
2.  **Layout Structure**: Stacked vertical scroll input panel framed in rich border lines.
3.  **Components**:
    *   *Text Fields*: Text boxes for Title and Description, using filled inputs.
    *   *Duration pickers*: Start-time and End-time calendar date select menus.
    *   *Category Color Select*: Segmented dots map to standard color values (Fitness, Finance, Learning).
    *   *Commute Buffer Switches*: Dual switches for "Schedules commute alarm (15m buffer)" and "Prep time (30m buffer)".
4.  **User Interactions**:
    *   Enter custom information.
    *   Toggling commute buffers automatically generates linked background alarm parameters.
5.  **Navigation Flow**: Back buttons reset inputs. Clicking "CREATE ROUTINE" locks parameters into database and pops back.
6.  **Empty States**: N/A.
7.  **Error States**: Highlights input boxes in crimson colors if the active start time is chosen to precede the current date.
8.  **Accessibility Considerations**: Provides explicit content descriptors clarifying buffer variables.

### Screen 11: Goal Dashboard
1.  **Screen Purpose**: Visual progression index tracking micro-commitments, habit goals, and user milestones.
2.  **Layout Structure**: Multi-column dashboard with asymmetrical grid panels showing progress metrics.
3.  **Components**:
    *   *Active Goal Progress Index*: Sleek list cards mapping active goals to percentage bars colored according to category schemas.
    *   *Summary Milestone Badges*: Grid blocks highlighting unlocked certificates (e.g. "Early Ascent Phase 1").
    *   *Create Button*: Clean, high-contrast action card pinned as a grid target.
4.  **User Interactions**:
    *   Tapping concrete Goal Cards deep-links to *Goal Details*.
    *   Scrolling through historic accomplishments.
5.  **Navigation Flow**: Links between other screens and details pages simply.
6.  **Empty States**: Displays encouraging prompt: "Target heights are reached step by step. Tap the action box to construct your initial goal paths".
7.  **Error States**: Employs clean state recovery routines if initial tracking indices mismatch.
8.  **Accessibility Considerations**: Numeric ratios are announced directly (e.g. "Completed milestones: 3 of 10").

### Screen 12: Goal Details
1.  **Screen Purpose**: In-depth progression analysis displaying checklist history, connected alarms, and analytics for a single goal.
2.  **Layout Structure**: Two-part split menu. Top part features milestone tracking charts; bottom part lists individual daily actions.
3.  **Components**:
    *   *Goal Tracker Vector Graphs*: Fine-point line charts charting cumulative progress using colored vector points.
    *   *Milestone Checklists*: Clean checked inputs mapping subtasks.
    *   *Linked Alarm Control Card*: Dedicated alarm panel detailing alarm connections for specific goals.
4.  **User Interactions**:
    *   Manually checking milestone tasks.
    *   Reconfiguring active alarm coordinates.
5.  **Navigation Flow**: Returns backwards to *Goal Dashboard*.
6.  **Empty States**: "No milestones configured. Design small, actionable targets to track progress".
7.  **Error States**: Localized validation messaging.
8.  **Accessibility Considerations**: Progress percentages are read dynamically during focus highlights.

### Screen 13: Habit Tracker
1.  **Screen Purpose**: Modern habit registry with detailed weekly completion grids to visualize daily streaks.
2.  **Layout Structure**: Clean list of compact horizontal strips optimized for high visual density.
3.  **Components**:
    *   *Habit Title Blocks*: Text structures showing custom descriptive titles alongside energetic streak flame icons.
    *   *Completion Grid Strip*: 7 horizontal day blocks per habit that light up in Brand Cyan when completed.
    *   *Quick Log Trigger*: Accessible circular completion buttons representing today's actions.
4.  **User Interactions**:
    *   Tap a daily check pip to complete/uncomplete a habit for today, triggering a delightful explosion of custom screen particles.
    *   Long tap triggers a popup to add brief performance logs or completion notes.
5.  **Navigation Flow**: Core bottom bar entry point.
6.  **Empty States**: Retro vector diagram of a growing plant seedling, labeled: "Habits define our paths. Cultivate your initial habit now".
7.  **Error States**: If two completions are triggered on a single calendar day, prevents double records and sounds a micro caution vibration.
8.  **Accessibility Considerations**: Active streak counts are explicitly communicated as TalkBack voice readouts.

### Screen 14: Daily Mission Screen
1.  **Screen Purpose**: List today's specific high-interest daily accomplishments to maintain focus.
2.  **Layout Structure**: Immersive single-column timeline focused on 3 specific "Keystone Missions".
3.  **Components**:
    *   *Keystone Mission Cards*: 3 large structured dark cards utilizing dynamic glowing cyan border line accents.
    *   *Daily Wake Performance*: Quick summaries of morning wake metrics.
    *   *Completion Counter Ring*: Central radial percentage meters tracking consolidated activities.
4.  **User Interactions**:
    *   Swipe cards horizontally to mark as reviewed.
    *   Slide checklist elements to complete.
5.  **Navigation Flow**: Back buttons or quick slide actions return user to Home screen.
6.  **Empty States**: Dynamic congratulatory graphics: "All Keystone Missions achieved. Your discipline is absolute!".
7.  **Error States**: Employs normal local error handlers.
8.  **Accessibility Considerations**: Generous 52.dp touch surfaces for all checklist targets.

### Screen 15: Sleep Dashboard
1.  **Screen Purpose**: Review offline sleep duration loggings, analysis, and calculated debt indices.
2.  **Layout Structure**: Scrollable dashboard incorporating rich custom visual data elements.
3.  **Components**:
    *   *Sleep Score Header Card*: Visual panels centered on large Share Tech Mono display values paired with color-coded status elements.
    *   *Circadian Cycle Wave Chart*: High-fidelity canvas drawing a smooth wave representing simulated circadian cycles.
    *   *Manual Sleep Log Form*: Expansion blocks with slide timers to quickly record unexpected sleep patterns.
4.  **User Interactions**:
    *   Enter custom coordinates inside manual logs.
    *   Scroll cumulative weekly charts to view historic sleep logs.
5.  **Navigation Flow**: Interactive paths to statistics modules.
6.  **Empty States**: Vector illustration of dark night skies: "No sleep logs registered. Use standard alarm triggers to initiate automatic sleep tracking".
7.  **Error States**: Out-of-bounds input warnings if manual sleep logs exceed 24 hours.
8.  **Accessibility Considerations**: Complex data visualization graphs utilize full fallback tabular data list descriptions.

### Screen 16: Statistics Dashboard
1.  **Screen Purpose**: Cumulative analysis panel highlighting average wake response speeds, overall habit frequencies, and sleep debts.
2.  **Layout Structure**: Multi-tab scrolling layout separating "Wake Stats", "Habit Streaks", and "Sleep Patterns".
3.  **Components**:
    *   *Response Speed Dial Gauges*: Dial displays plotting average wake speeds (in seconds).
    *   *Cumulative Activity Charts*: Multi-chart blocks tracking overall achievements over 30 days.
    *   *Insight Cards*: High-contrast notification panels showing calculated tips (e.g. "Your wake speed is 24% faster on Mondays").
4.  **User Interactions**:
    *   Select segment tabs to alternate views.
    *   Filter historic logs across 7-day, 30-day, and 90-day intervals.
5.  **Navigation Flow**: Interlocks directly with *Timeline View*.
6.  **Empty States**: Shows empty charts with description: "Data is gathered over consecutive days. Maintain steady habits to populate dashboard charts".
7.  **Error States**: Handles missing DB data elements by generating flat baseline coordinates gracefully.
8.  **Accessibility Considerations**: High-contrast chart layers are accompanied by screen-readable summaries.

### Screen 17: Timeline View
1.  **Screen Purpose**: Historical chronological log tracking all waking events, habit check-ins, and scheduled alarms in a single unified feed.
2.  **Layout Structure**: Beautiful vertical timeline list, using dotted vertical lines to anchor cards chronologically.
3.  **Components**:
    *   *Dotted Axis Guides*: Vertical line layouts representing chronological flow.
    *   *Activity Cards*: Compact cards color-coordinated by action type.
    *   *Search & Filter Bars*: Quick text searches and category toggles on the top strip.
4.  **User Interactions**:
    *   Interactive scroll through past history.
    *   Filter timeline items by selecting activity pill tags.
5.  **Navigation Flow**: Instantly links back to primary dashboards.
6.  **Empty States**: "The timeline is empty. Complete tasks to record events in your journey logs".
7.  **Error States**: Standard error recovery banners.
8.  **Accessibility Considerations**: Every entry contains comprehensive alt-labels detailing precise chronological activities.

### Screen 18: Settings
1.  **Screen Purpose**: Modify global theme parameters, notification priorities, and configuration options.
2.  **Layout Structure**: Organized vertical list groups separated by clean divider elements.
3.  **Components**:
    *   *Group Headers*: Small capital headings in Orbitron separating preference buckets.
    *   *Visual Skin Toggles*: Radial color buttons to flip styles.
    *   *Sub-Screen Item Buttons*: Selection elements deep-linking into advanced security and restore zones.
4.  **User Interactions**:
    *   Tap items to toggle preferences with micro-haptic clicks.
    *   Choose theme adjustments via immediate dynamic styling refresh.
5.  **Navigation Flow**: Deep-links to *Security* and *Backup & Restore*.
6.  **Empty States**: N/A.
7.  **Error States**: Local text indicators if device platform permissions are denied.
8.  **Accessibility Considerations**: Sliders explicitly announce current preferences and state labels clearly.

### Screen 9: Security (PIN Vault)
1.  **Screen Purpose**: Prevent casual tampering with alarm coordinates or habit milestones by enforcing a secure lock screen pattern.
2.  **Layout Structure**: Numeric dial pad layout framed in rich dark colors.
3.  **Components**:
    *   *Passcode Inputs*: 4 glowing circular indicators that light up in Brand Cyan when characters are key-entered.
    *   *Dial Pad*: Round, highly tactile numeric items (0-9) utilizing M3 ripple effects.
    *   *Instruction Copy*: "Enter Passcode to Unlatch Security Vault".
4.  **User Interactions**:
    *   Type PIN sequence. Correct PIN unlocks settings panels instantly.
    *   Toggle setting to "Password Protect Alarm Settings" using switch components.
5.  **Navigation Flow**: Clears lock screens backwards to parent panels upon validation.
6.  **Empty States**: N/A.
7.  **Error States**: Incorrect PIN input shakes the screen layout horizontally and flashes all 4 circles in pink `#FF006E` with a minor haptic error buzz.
8.  **Accessibility Considerations**: Large 64.dp tap matrices for all numeric buttons to support ease of navigation.

### Screen 20: Backup & Restore (Diagnostic Control)
1.  **Screen Purpose**: High-reliability offline data management, allowing exporting/importing JSON maps of the local database.
2.  **Layout Structure**: Vertical stack detailing process actions paired with readable file lists.
3.  **Components**:
    *   *Diagnostics Panel*: Status readouts displaying active backup counts and storage parameters.
    *   *Backup Action Buttons*: Primary gradient icons indicating "GENERATE JSON MAP" and "IMPORT RESTORE PATH".
    *   *Historic Backup File List*: File item list displaying localized backup entries.
4.  **User Interactions**:
    *   Tapping generate creates a structured database JSON export.
    *   Selecting database imports prompts clean structural confirmations.
5.  **Navigation Flow**: Pop confirmations, returning outwards to *Settings*.
6.  **Empty States**: File list displays: "No diagnostic JSON maps registered. Tap generate to create local backups".
7.  **Error States**: Import schema checking validates file formats; if corrupted, prints a clear, action-guided red message box explaining specific parsing errors.
8.  **Accessibility Considerations**: Offers visual progress bar loaders during asynchronous imports.

---

## 4. WIDGETS & SYSTEM ALARM RELIABILITY ARCHITECTURE

To ensure ARISE runs with absolute consistency, we deploy an **Android Alarm Reliability Architecture** running fully on-device with zero reliance on remote servers:

```
                  ┌───────────────────────────────┐
                  │      AlarmManager (Exact)     │◄─── Schedules physical device wake wake
                  └───────────────┬───────────────┘
                                  │
                                  ▼
┌─────────────────────────────────┼─────────────────────────────────┐
│         BootReceiver            │          WorkManager            │
├─────────────────────────────────┼─────────────────────────────────┤
│ Re-registers active schedules   │ Handles gradual state logs,     │
│ dynamically after reboot cues   │ exports backup JSON maps,       │
│                                 │ and monitors periodic cues      │
└─────────────────────────────────┴─────────────────────────────────┘
```

1.  **Exact Scheduling (`AlarmManager.setAlarmClock()`)**:
    *   We leverage the exact Android scheduling system to bypass Doze Mode power management configurations.
    *   Displays direct alarm clock icons in the system status bar, ensuring priority background task execution.
2.  **Reliability Reboots (`BootReceiver`)**:
    *   Listens for `Intent.ACTION_BOOT_COMPLETED`.
    *   Triggers background checks to query Room databases and restore active schedule profiles immediately.
3.  **Reliability Logging (`WorkManager`)**:
    *   Executes lightweight periodic jobs to manage historic JSON file retention, compile statistics trends, and verify overall integrity offline.

---

## 5. COMPLETE USER JOURNEY: WAKING TO MEDITATION ROADMAP

```
[Morn: 07:00 AM] ──► [Math Challenge] ──► [Home Dashboard] ──► [Habits Log]
                                                                        │
                                                                        ▼
[Sleep Log] ◄── [Bedtime Alarm] ◄── [Goals Check] ◄── [Calendar Schedule]
```

### 🌄 Step 1: Waking Up (The Active Ascent)
*   **Time: 07:00 AM**
*   *Action*: The custom scheduled crescendo audio begins playing. The screen illuminates, bypassing system lock views to display **Screen 6: Alarm Challenge Screen**. The room strobe light flashes to assist waking.
*   *Task*: The user is greeted with a mathematical equation in crisp Orbitron typography. They cannot bypass this view with the physical home button. Tipping and solving the mathematical problem silences the alarm audio.
*   *Outcome*: The success interface logs the performance inside the local Room database using the **AlarmHistoryItem** entity and proceeds directly to the home screen.

### ☀️ Step 2: Morning Command Review (The Focus Phase)
*   **Time: 07:15 AM**
*   *Action*: **Screen 2: Home Dashboard** registers as the primary screen. The user reviews their wake metrics: a sleep score of `86%`, their active schedule timeline highlighting "09:30 AM Engineering Sync", and today's top 3 Daily Missions.
*   *Task*: The user opens **Screen 13: Habit Tracker** and marks "Drink Water" and "Stretch" as complete. The UI bursts with energetic visual particle effects.

### 💼 Step 3: Daylight Cadence (The Mastery Phase)
*   **Time: 09:30 AM – 05:00 PM**
*   *Action*: Throughout the day, the user follows scheduling timelines via widgets on their home screens.
*   *Task*: Completing milestones for "Build Project Architecture" updates **Screen 12: Goal Details**, keeping weekly charts progressing and calculating overall goals metrics on the fly.

### 🌃 Step 4: Circadian Ascent (The Reset Phase)
*   **Time: 09:30 PM – 10:30 PM**
*   *Action*: An educational bedtime alarm gently rings, alerting the user to shut down active focus modules.
*   *Task*: The user logs their evening habits, reviews long-term growth trends in **Screen 16: Statistics Dashboard**, and triggers manual backups on **Screen 20: Backup & Restore** for offline peace of mind.
*   *Outcome*: The user locks progress, sets the "Bedside Mode" to dim the screen interface to a warm, eye-safe glow, and drifts off, ready to arise and run the cycle once again.
