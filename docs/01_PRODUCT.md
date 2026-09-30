# DAILY BATTLE — PRODUCT SPECIFICATION

**Document:** 01_PRODUCT.md  
**Product:** Daily Battle  
**Platform:** Android  
**Document Status:** PRODUCT SPECIFICATION  
**Specification Version:** 1.0  
**Last Updated:** 2026-09-30  
**Parent Document:** 00_MASTER_SPEC.md  

---

# 1. PURPOSE

This document defines the product-level behavior and experience of Daily
Battle.

It explains:

- what Daily Battle is
- the product problem it addresses
- the product promise
- the intended user experience
- the core daily loop
- the emotional loop
- the product personality
- the competition model
- the social model
- the practice model
- the role of AI
- the MVP scope
- the explicit product boundaries

This document describes product intent.

It does not define detailed:

- gameplay algorithms
- screen layouts
- Android architecture
- database schemas
- API contracts
- analytics implementation
- animation specifications

Those areas are defined in the corresponding documents under `/docs`.

---

# 2. PRODUCT OVERVIEW

Daily Battle is a short-form daily competitive skill game built around one
official daily Battle.

The product gives the player:

- three short challenges
- approximately three minutes of gameplay
- one combined score
- personal performance context
- friend competition
- rival comparison
- a reason to return the following day

The central product idea is:

> 3 challenges. ~3 minutes. One score.

Daily Battle is designed to become a repeatable daily ritual rather than an
infinite gaming session.

---

# 3. PRODUCT PROMISE

## 3.1 Core Promise

> 3 challenges. ~3 minutes. One score.

The promise communicates three things:

### 3 challenges

The player knows the official Battle is finite.

### ~3 minutes

The product is designed to fit into a short daily session.

### One score

The three challenges resolve into one meaningful daily result.

---

# 4. PRODUCT PROBLEM

Daily Battle is designed around a simple product opportunity:

A player should be able to open an application, complete a meaningful
competitive experience in a short period of time, understand how they
performed, compare themselves with people they care about, and leave.

The experience should not require:

- long sessions
- complex progression systems
- extensive onboarding
- continuous social consumption
- complicated game economies
- large amounts of content browsing

The product should make the daily action immediately understandable.

---

# 5. PRODUCT EXPERIENCE

The intended product experience is:

```text
Open
  ↓
Understand today's Battle
  ↓
Play
  ↓
Receive score
  ↓
Understand performance
  ↓
Compare
  ↓
Finish
  ↓
Return tomorrow

The product should feel like a short competitive ritual.

The user should not need to spend significant time deciding what to do.

The primary action should remain obvious.

6. CORE DAILY LOOP

The core loop is:

1. Open Daily Battle
2. See today's Battle
3. Start the official Battle
4. Complete Snap
5. Complete Shift
6. Complete Crowd Call
7. Receive the combined score
8. Understand personal performance
9. Compare with rival/friends
10. Finish the session
11. Return the next day

The loop should be short enough that completing the official Battle feels
manageable even when the user has limited time.

7. CORE EMOTIONAL LOOP

The intended emotional progression is:

Anticipation
    ↓
Play
    ↓
Focus
    ↓
Tension
    ↓
Score Reveal
    ↓
Self Comparison
    ↓
Friend Rivalry
    ↓
Done
    ↓
Come Back Tomorrow

Each stage should have a corresponding product purpose.

7.1 Anticipation

The Home experience should make today's Battle feel available and worth
playing.

The player should understand:

there is a Battle today
it is short
it has three challenges
it is the main thing to do

The exact challenge types should not necessarily be revealed before the
Battle when the product specification intentionally keeps them unknown.

7.2 Play

The player enters the official Battle.

The experience should transition from dashboard-like navigation into focused
gameplay.

7.3 Focus

Each challenge should remove unnecessary interface elements.

Gameplay should become the dominant experience.

7.4 Tension

Challenge feedback, timing, decisions, and score progression should create
a sense of meaningful performance.

The product should not create tension through unnecessary punishment or
confusing UI.

7.5 Score Reveal

The final score is a reward moment.

The score should receive visual priority.

The player should immediately understand the outcome.

7.6 Self Comparison

The product should help the player understand:

how they performed
whether they improved
whether they reached a personal best
how they compare with their previous performance
7.7 Friend Rivalry

The player can compare the result with friends or a current rival.

The rivalry should be personal rather than a global ranking experience.

7.8 Done

The player should be able to leave after completing the Battle.

The product should not require an endless browsing session.

7.9 Come Back Tomorrow

The finite nature of the daily Battle should create anticipation for the
next Battle.

The product should communicate completion rather than immediately pushing
the player into another endless activity.

8. PRODUCT PERSONALITY

Daily Battle should communicate the following personality:

Fast
Competitive
Premium
Intelligent
Playful
Social

These characteristics should influence product language, visual hierarchy,
interaction design, and feedback.

9. PERSONALITY DEFINITIONS
9.1 Fast

The product should feel responsive.

The user should quickly understand:

what is happening
what they need to do
what happened after an action

Avoid unnecessary steps.

9.2 Competitive

Competition should be visible through:

score
personal best
improvement
percentile
rival comparison
friend scores

Competition should not require a global leaderboard.

9.3 Premium

The product should feel deliberate and polished.

Premium direction includes:

restrained visual hierarchy
strong typography
consistent spacing
purposeful motion
clear interaction states
limited visual noise

Premium does not mean excessive decoration.

9.4 Intelligent

The product should feel thoughtfully designed.

It should avoid unnecessary explanation while remaining understandable.

The product should not claim scientific intelligence measurement.

9.5 Playful

Gameplay should have energy and feedback.

Playfulness should come from:

interaction
challenge
feedback
motion
competition

It should not require childish visual design.

9.6 Social

The product should give users reasons to compare themselves with friends.

Social behavior should remain focused on competition rather than becoming a
general-purpose social network.

10. TARGET EXPERIENCE

The intended user should be able to understand the product quickly.

The user should understand:

There is one Battle today.
It has three challenges.
It takes about three minutes.
I get one score.
I can compare that score with myself and my friends.

The interface should communicate this without requiring lengthy instructions.

11. PRODUCT INFORMATION HIERARCHY

Daily Battle follows a three-level information hierarchy.

Level 1 — What should I do?

Examples:

Play Battle
Start challenge
Choose answer
Continue
Beat rival

This is the most important information.

Level 2 — What matters to me?

Examples:

score
personal best
improvement
percentile
rival gap
momentum
Level 3 — Supporting Information

Examples:

history
detailed breakdown
settings
secondary metadata

Supporting information must not overpower the primary action.

12. CORE PRODUCT STRUCTURE

The product is organized around the following experience areas:

Onboarding
    ↓
Home
    ↓
Battle
    ↓
Results
    ↓
Rival
    ↓
Friends
    ↓
Profile
    ↓
History
    ↓
Settings

The detailed screen architecture is defined separately in:

03_SCREEN_ARCHITECTURE.md

13. ONBOARDING PRODUCT EXPERIENCE

The initial onboarding experience should be lightweight.

The currently defined onboarding flow is:

Welcome
  ↓
Battle Name
  ↓
Home

The purpose is to get the user into the core product quickly.

The product should not require an unnecessarily long tutorial before the
first Battle.

Detailed onboarding behavior belongs in:

03_SCREEN_ARCHITECTURE.md

and

04_SCREEN_BLUEPRINTS.md

14. HOME PRODUCT EXPERIENCE

Home is the entry point to the daily ritual.

Its dominant purpose is:

Get the player into today's Battle.

The Home experience should communicate:

user identity/greeting
momentum
today's Battle
rival context

The primary focus must remain today's Battle.

15. TODAY'S BATTLE

Today's Battle is the primary product object.

The Home experience should communicate:

TODAY'S BATTLE
3 challenges
approximately 3 minutes
official Battle status
action to play

The Home experience should not overload the user with detailed statistics.

16. BATTLE PRODUCT EXPERIENCE

The Battle is a focused gameplay session.

The current official sequence is:

Snap
  ↓
Shift
  ↓
Crowd Call

Each challenge represents a different type of interaction.

The Battle should feel cohesive as one experience while allowing each
challenge to have its own identity.

17. CHALLENGE PRODUCT ROLES
17.1 Snap

Product role:

Reaction

Snap represents fast reaction and response.

The experience should be:

fast
clean
focused
visually restrained
17.2 Shift

Product role:

Memory / Precision

Shift represents memory and precision.

The experience should feel mechanically and visually distinct from Snap.

17.3 Crowd Call

Product role:

Social Prediction

Crowd Call represents prediction and social intuition.

The experience should feel more socially expressive than the other two
challenges.

18. RESULT PRODUCT EXPERIENCE

Results are the primary reward moment after the Battle.

The product should answer:

How did I do?
How does that compare to my previous performance?
How did each challenge contribute?
How close am I to my rival?

The most important piece of information is the final score.

The result experience should not become an analytics dashboard.

19. PERSONAL PERFORMANCE MODEL

Daily Battle emphasizes personal performance.

Important performance concepts include:

score
personal best
average
improvement
percentile
momentum
recent Battles
challenge-specific performance

The purpose is to help the user understand their own progression.

20. MOMENTUM

Momentum is a product concept intended to support repeat participation.

Momentum should encourage continuity without creating an unnecessarily
punitive experience.

The exact momentum/streak calculation is not defined in this document.

Status:

PENDING

Detailed rules must be defined in:

02_GAMEPLAY.md

before implementation.

21. PERSONAL BEST

Personal best is a meaningful achievement state.

The product may highlight:

new personal best
score improvement
challenge improvement

Personal best should feel rewarding without requiring a separate currency
system.

22. PERCENTILE

Percentile provides competitive context without requiring a global
leaderboard.

It should help answer:

How did my performance compare with the broader player population?

The implementation and calculation source must be defined in the technical
and data specifications.

The product should not turn percentile into a complex ranking dashboard.

23. FRIEND COMPETITION MODEL

Friends exist primarily to support competition.

The user should be able to:

add friends
view friend scores
compare performance
identify a rival
attempt to beat a rival

The product should not require a general social feed to support this.

24. RIVAL MODEL

The Rival experience simplifies social competition.

The intended model is:

One person. One gap. One goal.

The user should understand:

who the rival is
what the rival scored
what the user scored
the difference
what action can be taken

The Rival screen should not become a replacement for the Friends screen.

25. SOCIAL PRODUCT BOUNDARIES

The social system is intentionally limited.

Included
Friends
Friend scores
Rival comparison
Battle Code
Add Friend
Personal competitive context
Excluded from MVP
Likes
Followers
Comments
Social posts
General social feed
Direct chat
General-purpose social networking

These boundaries are intentional product constraints.

26. BATTLE CODE

Battle Code is the defined mechanism for adding friends.

The product concept includes:

user's own Battle Code
copy Battle Code
enter another user's Battle Code
add friend

The following are not part of the current defined MVP friend-addition
experience:

contact permission
phone-number search
location-based friend discovery

The exact data and backend behavior will be defined later.

27. PRACTICE PRODUCT MODEL

Practice exists separately from the Official Battle.

Its purpose is to allow the player to:

learn
experiment
improve
practice individual skills

Practice should not undermine the value of the daily official Battle.

Practice must not:

replace the official attempt
alter the official score
manipulate official ranking/percentile
be confused with the daily Battle
28. OFFICIAL VS PRACTICE

The distinction should be obvious.

Official language may include:

TODAY
READY
OFFICIAL

Practice language may include:

PRACTICE

The product should not use:

LIVE

unless the feature genuinely represents live multiplayer interaction.

29. OFFICIAL BATTLE CONSISTENCY

The official Battle should be consistent for the player population.

The same official challenge set should be presented to everyone.

Personalization may be used for practice or other explicitly approved
systems.

Personalization must not silently create different official gameplay for
different users.

30. AI PRODUCT ROLE

AI is supporting infrastructure.

AI may be used where explicitly approved for:

practice personalization
adaptive practice
content support
intelligent supporting systems
backend/product infrastructure

AI should not dominate the visible product identity.

Avoid:

excessive AI labels
generic AI marketing language
unnecessary AI assistant UI
AI-generated-looking visual patterns
presenting ordinary product behavior as "AI"

The user should experience the product as Daily Battle, not as an AI demo.

31. REAL-WORLD CHALLENGE MODEL

Real-world challenges / Wild Card concepts are not part of the MVP core
experience.

They may be considered later through an explicit product decision.

They must not be independently introduced during implementation.

32. PRODUCT REWARD MODEL

The product's primary rewards are performance-based.

Examples include:

score
improvement
personal best
percentile
rival progress
momentum

The product does not currently define a currency-based reward system.

Coins are explicitly excluded from the MVP product direction.

33. MONETIZATION PRODUCT BOUNDARY

The final monetization strategy has not yet been defined.

Status:

PENDING

Until a monetization decision is explicitly approved, implementation must
not introduce:

advertisements
subscriptions
paid retries
coins
energy systems
Battle passes
pay-to-win mechanics
premium challenges
cosmetic economies

Any future monetization system must be documented in:

13_MONETIZATION.md

and approved before implementation.

34. RETENTION MODEL

Daily Battle's primary retention concept is the daily ritual.

The product should create a reason to return through:

the next daily Battle
personal improvement
momentum
personal bests
friend rivalry
score comparison

Retention should not depend on:

infinite content
artificial resource depletion
excessive notifications
forced social engagement
arbitrary reward currencies
35. SESSION MODEL

The official Battle is intentionally short.

Target duration:

Approximately 3 minutes.

The player should be able to complete the official Battle without needing
a long uninterrupted gaming session.

The product should not intentionally stretch the official Battle through
unnecessary screens or secondary interactions.

36. DAILY FINITE MODEL

The official Battle is finite.

Once the player completes today's official Battle:

Today's Battle
      ↓
Completed
      ↓
Result
      ↓
Comparison
      ↓
Done
      ↓
Tomorrow

The product should not immediately replace the completed Battle with an
infinite stream of additional mandatory gameplay.

37. GLOBAL LEADERBOARD BOUNDARY

The product intentionally avoids a global leaderboard.

Instead, competitive context comes from:

percentile
friends
rival
personal improvement

This keeps competition meaningful without requiring the player to compete
against an enormous anonymous ranking list.

38. PRODUCT LANGUAGE PRINCIPLES

Product language should be:

concise
direct
competitive
understandable
personal

Examples of action language:

PLAY BATTLE
BEAT RIVAL
ADD FRIEND
START PRACTICE
SHARE RESULT

Avoid unnecessarily technical or generic UI language.

39. PERSONAL LANGUAGE

Competition should feel personal.

Preferred concepts include:

Your Score
Your Best
Your Rival
You
Your Progress
Beat Rahul

The product should not rely on impersonal global ranking terminology as its
primary motivational language.

40. PRODUCT VISUAL DIRECTION

The product direction is:

Premium interactive product with gaming energy.

The intended balance is approximately:

75–80% premium product design
20–25% game energy

The product should preserve:

premium hierarchy
strong typography
dark-first visual identity
controlled semantic color
focused gameplay
meaningful motion
41. VISUAL THINGS TO AVOID

The product should avoid:

generic esports aesthetics
cyberpunk aesthetics
casino aesthetics
childish game visuals
excessive neon
excessive purple glow
excessive glassmorphism
giant glow effects
generic AI-purple-gradient aesthetics
excessive stat cards
dashboard-heavy Home
social-feed layouts
fake scientific brain-training aesthetics
unnecessary decorative UI
42. PRODUCT DENSITY

Daily Battle should prioritize clarity over information density.

A screen should not contain information merely because it is available.

The product should answer:

What does the player need to know right now?

Supporting information should remain subordinate.

43. CARD USAGE

Cards should be used intentionally.

A card is appropriate when it improves:

grouping
hierarchy
scanning
interaction
comparison

Cards should not be nested unnecessarily.

The product should avoid:

Card
  → Card
    → Card
      → Card

without a clear information-hierarchy reason.

44. GAMEPLAY PRODUCT DENSITY

Gameplay screens should contain only the information needed to perform the
challenge.

Avoid:

social controls
profile controls
unnecessary statistics
navigation clutter
decorative dashboards
unnecessary secondary actions

The challenge should dominate the screen.

45. RESULTS PRODUCT DENSITY

Results may contain more information than gameplay because the player is now
in an analysis/reward state.

However, Results should still prioritize:

Score
Percentile
Improvement
Breakdown
Rival

The product should not present every possible statistic simultaneously.

46. PROFILE PRODUCT PURPOSE

Profile exists to help the user understand themselves as a player.

The concept is:

Battle DNA

It may communicate:

Speed
Memory
People
best score
average
momentum
records

The Profile should not become:

an analytics dashboard
a scientific assessment
an IQ report
47. HISTORY PRODUCT PURPOSE

History exists to help the player understand progress over time.

The intended content includes:

recent Battles
improvement
best score
average
momentum
a focused progress visualization

History should remain focused rather than becoming a large analytics system.

48. SETTINGS PRODUCT PURPOSE

Settings is intentionally calm and simple.

It should provide access to:

sound
haptics
notifications
Battle name
Battle Code
privacy
account-related controls
delete account
terms
about

Settings should not feel like another game screen.

49. NOT A GENERIC GAME

Daily Battle should not become a collection of unrelated game mechanics.

Every gameplay feature should support the core product promise:

3 challenges. ~3 minutes. One score.

A proposed feature should be questioned if it:

significantly increases session length
introduces unnecessary complexity
weakens the daily ritual
creates a second primary loop
requires an unrelated economy
turns the product into a social network
makes the product feel like a generic game collection
50. NOT A SOCIAL NETWORK

Social features exist to strengthen competition.

The product should not optimize for:

content creation
likes
followers
comments
passive scrolling
endless social consumption

The primary social action is:

Compare and compete.

51. NOT AN ANALYTICS DASHBOARD

Daily Battle contains performance data, but it is not an analytics product.

Analytics should support the player rather than become the experience.

The product should prioritize:

Do
↓
Score
↓
Understand
↓
Compare

rather than:

Open
↓
Read charts
↓
Read statistics
↓
Read more statistics
↓
Maybe play
52. NOT AN INFINITE GAME

The official daily Battle is finite.

The product should preserve the value of completion.

After completion, the user should feel:

I finished today's Battle.

rather than:

I still need to keep playing to make progress.

53. PRODUCT SUCCESS MODEL

At the product level, Daily Battle succeeds when the experience allows a
player to repeatedly complete the intended ritual:

Return
↓
Play
↓
Understand performance
↓
Compare
↓
Finish
↓
Return again

Specific business metrics and analytics events are not defined in this
document.

Those belong in:

14_ANALYTICS.md

54. MVP FEATURE BOUNDARY
Included
Onboarding
Welcome
Battle Name
Home
Today's Battle
Battle Intro
Snap
Shift
Crowd Call
Results
Rival
Friends
Add Friend
Profile
Battle DNA
History
Settings
Official Battle
Practice separation
Personal score
Percentile
Personal best
Momentum concept
Friend rivalry
Battle Code
Required loading states
Required empty states
Required error states
Required completion states
55. MVP EXCLUDED
Global leaderboard
Coins
General-purpose chat
Infinite social feed
Likes
Followers
Comments
Photo proof as primary game mechanic
Generic quiz system
IQ claims
Camera-first experience
Real-time multiplayer
Excessive AI branding
Fake LIVE status
Large dashboard experience
Unapproved monetization
Unapproved reward economy
Unapproved Wild Card / real-world gameplay
56. PRODUCT DECISION BOUNDARIES

The following decisions are not finalized by this document.

They must be explicitly resolved before their respective implementations.

56.1 User Identity

Status:

PENDING

The product currently defines Battle Name and Battle Code, but the complete
identity/authentication model is not yet specified.

56.2 Rival Assignment

Status:

PENDING

The product defines the Rival concept but not the exact algorithm for
selecting or assigning a rival.

56.3 Momentum Calculation

Status:

PENDING

The product defines momentum as a concept but not the exact formula.

56.4 Friend Lifecycle

Status:

PENDING

The product defines adding friends through Battle Code but does not yet
fully define:

pending state behavior
acceptance
blocking
removal
synchronization
56.5 Percentile Calculation

Status:

PENDING

The product defines percentile as a competitive context but not the exact
calculation or backend source.

56.6 Content Generation

Status:

PENDING

The product defines official Battle consistency but does not yet define
the exact content-generation pipeline.

56.7 Monetization

Status:

PENDING

No monetization implementation should begin until explicitly defined.

57. PRODUCT PRINCIPLES CHECKLIST

Every new product feature must be evaluated against:

[ ] Does it strengthen the 3-minute daily ritual?

[ ] Does it support the core promise?

[ ] Does it have a clear product purpose?

[ ] Does it preserve the finite daily Battle?

[ ] Does it preserve the importance of one official score?

[ ] Does it support personal improvement or meaningful competition?

[ ] Does it preserve the friend-rivalry model?

[ ] Does it avoid becoming a social network?

[ ] Does it avoid becoming an infinite feed?

[ ] Does it avoid unnecessary game economy?

[ ] Does it avoid unnecessary complexity?

[ ] Does it fit the premium product direction?

[ ] Does it preserve focused gameplay?

[ ] Does it avoid unsupported scientific/IQ claims?

[ ] Does it avoid unnecessary AI branding?

[ ] Does it require an explicit product decision?

If the final answer is unclear, the feature should be marked:

DECISION_REQUIRED

rather than being implemented automatically.

58. PRODUCT CHANGE RULE

The product specification is not changed by implementation convenience.

If implementation reveals that a requirement is difficult, the requirement
does not automatically change.

Instead:

Requirement
    ↓
Technical problem identified
    ↓
Problem documented
    ↓
Options proposed
    ↓
Decision made
    ↓
Documentation updated
    ↓
Implementation continues

Implementation convenience alone is not sufficient justification for a
product change.

59. PRODUCT SCOPE RULE

A feature is not part of Daily Battle merely because:

another game has it
another app has it
an AI agent thinks it would improve engagement
it is technically easy to implement
it is visually attractive
it is common in mobile games

A feature must have an explicit product purpose and approved scope.

60. PRODUCT NORTH STAR

The central product question is:

Can the player understand, complete, and value today's Battle in
approximately three minutes?

Every major product decision should preserve this principle.

61. PRODUCT SUMMARY

Daily Battle is:

A premium dark-first daily competitive skill game.

One official Battle per day.

Three short challenges.

Approximately three minutes.

One combined score.

Personal performance.

Percentile context.

Friend rivalry.

Personal improvement.

A reason to return tomorrow.
62. PRODUCT CONSTITUTION

The following statements are considered foundational product principles:

Daily Battle is finite.

Daily Battle is daily.

The official Battle has three challenges.

The official Battle produces one score.

The official Battle is approximately three minutes.

The official Battle has one official attempt.

The official Battle is consistent across players.

Practice is separate from the official Battle.

Practice does not alter the official result.

Personal performance matters.

Friend rivalry matters.

The product is not a global leaderboard.

The product is not a social network.

The product is not a generic quiz.

The product is not an IQ test.

The product is not camera-first.

The product is not real-time multiplayer.

The product does not depend on coins.

The product does not depend on an infinite feed.

AI is supporting infrastructure.

The daily ritual is the primary product loop.

Gameplay should be focused.

Results should feel rewarding.

Rivalry should feel personal.

The user should be able to finish today's Battle.

The product should give the user a reason to return tomorrow.
63. DOCUMENT STATUS

Current product specification status:

Product Identity:
DEFINED

Core Promise:
DEFINED

Core Loop:
DEFINED

Emotional Loop:
DEFINED

Product Personality:
DEFINED

Official Battle Concept:
DEFINED

Challenge Roles:
DEFINED

Personal Performance Concept:
DEFINED

Friend/Rival Concept:
DEFINED

Practice Concept:
DEFINED

AI Product Role:
DEFINED

MVP Boundaries:
DEFINED

Explicitly Excluded Features:
DEFINED

Authentication:
PENDING

Rival Assignment:
PENDING

Momentum Formula:
PENDING

Friend Lifecycle:
PENDING

Percentile Calculation:
PENDING

Content Generation:
PENDING

Monetization:
PENDING

Analytics:
PENDING
64. SOURCE-OF-TRUTH RULE

This document defines product intent.

For implementation:

gameplay details → 02_GAMEPLAY.md
screen structure → 03_SCREEN_ARCHITECTURE.md
screen UI → 04_SCREEN_BLUEPRINTS.md
design tokens/components → 05_DESIGN_SYSTEM.md
navigation → 06_NAVIGATION_AND_FLOWS.md
technical architecture → 07_TECHNICAL_ARCHITECTURE.md
data/API → 08_DATA_AND_API.md
offline/errors → 09_OFFLINE_AND_ERROR_HANDLING.md
motion/haptics → 10_ANIMATION_HAPTICS.md
accessibility → 11_ACCESSIBILITY.md
testing → 12_TESTING_AND_QA.md
monetization → 13_MONETIZATION.md
analytics → 14_ANALYTICS.md
AI implementation governance → 15_ANTIGRAVITY_RULES.md
completion requirements → 16_DEFINITION_OF_DONE.md

No document should silently override this product specification.

If a conflict exists, follow the change-control process defined in
00_MASTER_SPEC.md.

65. FINAL PRODUCT STATEMENT

Daily Battle should feel like a small but meaningful daily competition.

The player opens the app.

They know what today's Battle is.

They play three challenges.

They receive one score.

They understand how they performed.

They see how they compare.

They finish.

And tomorrow, there is another Battle.

