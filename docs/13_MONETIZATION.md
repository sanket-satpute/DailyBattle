# Daily Battle — Monetization Specification

**Document:** `13_MONETIZATION.md`  
**Status:** LOCKED as monetization governance; unresolved commercial decisions remain PENDING  
**Product:** Daily Battle  
**Platform:** Android  
**Product authority:** User / Product Owner  
**Implementation agent:** Antigravity  

**Related documents:**
- `00_MASTER_SPEC.md`
- `01_PRODUCT.md`
- `02_GAMEPLAY.md`
- `03_SCREEN_ARCHITECTURE.md`
- `04_SCREEN_BLUEPRINTS.md`
- `05_DESIGN_SYSTEM.md`
- `06_NAVIGATION_AND_FLOWS.md`
- `07_TECHNICAL_ARCHITECTURE.md`
- `08_DATA_AND_API.md`
- `09_OFFLINE_AND_ERROR_HANDLING.md`
- `10_ANIMATION_HAPTICS.md`
- `11_ACCESSIBILITY.md`
- `12_TESTING_AND_QA.md`

---

# 1. Purpose

This document defines the monetization boundaries for Daily Battle.

Monetization must support the core product rather than distort it.

Daily Battle is fundamentally:

> **3 challenges. ~3 minutes. One score.**

The monetization system must therefore preserve:

- the finite Daily Battle,
- competitive fairness,
- friend-vs-friend competition,
- personal improvement,
- short-session design,
- premium product positioning,
- clear UX,
- user trust.

Monetization must never become the primary product experience.

---

# 2. Monetization Philosophy

Daily Battle should monetize around the **product experience**, not around frustration.

The product must not deliberately make the free experience feel broken so that users are pressured into paying.

The monetization system should avoid:

- pay-to-win mechanics,
- score purchases,
- paid retries for official Battle,
- energy systems,
- lives,
- arbitrary timers designed to force payment,
- coin-heavy economies,
- loot boxes,
- gambling-like mechanics,
- excessive interstitial interruptions,
- fake urgency,
- manipulative purchase flows.

---

# 3. Existing Product Constraints

The following product decisions are already established.

## 3.1 No coins

Daily Battle does not use a generalized coin/currency system.

Do not introduce:

```text
Coins
Gems
Tokens
Energy
Lives
Tickets

unless explicitly approved as a future product change.

4. Official Battle Must Remain Fair

The Official Daily Battle is the central competitive experience.

All users participating in the same Daily Battle should be evaluated against the same official challenge set according to the approved gameplay rules.

Monetization must not allow a user to purchase:

a higher score,
additional score,
easier official challenges,
better challenge conditions,
extra official attempts,
opponent manipulation,
percentile manipulation,
ranking manipulation.
5. No Pay-to-Win

The following are explicitly prohibited:

Pay for +100 score
Pay for score multiplier
Pay for easier challenge
Pay for extra official attempt
Pay for better percentile
Pay for automatic perfect answer
Pay to defeat rival
Pay for advantage during Snap
Pay for advantage during Shift
Pay for advantage during Crowd Call

The competitive result must remain skill-based.

6. Official Attempt Integrity

The official Daily Battle should normally remain a finite, meaningful attempt.

Monetization must not undermine the meaning of:

One official Battle.

Therefore:

Official Battle
    ↓
Play
    ↓
Complete
    ↓
Score

must not become:

Official Battle
    ↓
Fail
    ↓
Pay
    ↓
Retry
    ↓
Replace score

unless a future product decision explicitly changes the official-attempt model.

7. Practice and Monetization

Practice is conceptually separate from Official Battle.

Practice may eventually provide monetizable functionality, provided it does not modify official competitive results.

Potential future monetization areas may include:

additional practice modes,
expanded practice history,
advanced personal insights,
customization,
additional challenge variations,
optional premium experiences.

These are POSSIBLE, not approved features.

Antigravity must not implement them without explicit authorization.

8. Monetization Model Status

At the current stage, the exact commercial model is NOT LOCKED.

The following models are therefore considered candidates rather than requirements:

Model	Status
Free core experience	LOCKED
No pay-to-win	LOCKED
No coin economy	LOCKED
Subscription	PENDING
One-time premium purchase	PENDING
Cosmetic purchases	PENDING
Ads	PENDING
Rewarded ads	PENDING
Premium practice	PENDING
Premium analytics/insights	PENDING
Battle customization	PENDING
Premium themes	PENDING

Do not implement any PENDING monetization mechanism yet.

9. Free Core Experience

The core Daily Battle experience must remain meaningful without payment.

At minimum, the product's core identity must remain accessible:

Open app
→ Today’s Battle
→ Snap
→ Shift
→ Crowd Call
→ Results
→ Rival comparison

Monetization must not remove the central reason users return every day.

10. Ads

Advertising is currently PENDING.

If ads are eventually approved, they must be evaluated against the product's short-session nature.

The following must be explicitly decided before implementation:

whether ads exist,
ad format,
placement,
frequency,
cooldown,
whether ads appear before Battle,
whether ads appear after Battle,
whether ads appear inside gameplay,
whether ads appear on Results,
whether ads appear in Friends,
whether ads appear in Profile/History,
whether premium removes ads.
11. Gameplay Advertising Rule

Ads should not interrupt an active official challenge.

Do not place advertising:

during Snap
during Shift
during Crowd Call

unless a future product specification explicitly approves such behavior.

Gameplay should remain focused.

12. Interstitial Advertising

If interstitial advertising is approved later, it requires explicit product-level definition.

At minimum define:

Trigger
Frequency
Cooldown
Eligible screens
Excluded screens
Dismiss behavior
Failure behavior
Offline behavior
Premium behavior

Antigravity must not decide these values independently.

13. Rewarded Advertising

Rewarded ads are PENDING.

If considered later, the reward must not create competitive advantage in Official Battle.

For example, avoid:

Watch ad → retry official challenge
Watch ad → increase official score
Watch ad → multiply official score
Watch ad → reveal correct answer

Potential non-competitive rewards could be evaluated separately, but they require product approval.

14. Subscription

Subscription monetization is PENDING.

If a subscription is eventually introduced, its value proposition should be centered around optional premium functionality rather than competitive advantage.

Potential categories for evaluation:

Advanced personal insights
Expanded history
Additional practice
Customization
Ad-free experience
Premium visual themes

These are examples only.

They are not approved features.

15. One-Time Premium Purchase

A one-time premium purchase is also PENDING.

Possible future positioning:

Daily Battle Premium

could theoretically unlock a defined collection of non-competitive features.

However, no purchase package, price, feature set, or entitlement is currently locked.

16. Cosmetic Monetization

Cosmetic monetization is PENDING.

Potential categories include:

profile customization,
themes,
visual accents,
avatar customization,
non-competitive presentation options.

Cosmetics must not alter:

challenge difficulty,
score,
timing,
percentile,
official Battle eligibility,
rival matching.
17. Premium Personalization

Daily Battle contains a personal-performance concept through:

momentum,
best score,
average,
Battle DNA,
history,
rival comparison.

Advanced personalization may eventually become a premium value area.

However:

Basic understanding of the user's performance must not be intentionally made unusable for free users unless explicitly approved.

18. Battle DNA Monetization Boundary

Battle DNA must not become a misleading paid "scientific assessment."

The product must continue to avoid unsupported claims such as:

Your IQ is...
Your brain score is...
You are scientifically smarter than...

Premium insights, if introduced, must remain grounded in actual recorded game performance.

19. Rivalry and Monetization

Rivalry is a core retention mechanic.

It must not become a mechanism for monetization pressure.

Avoid:

Pay to beat rival
Pay to protect score
Pay to see rival score
Pay to rematch official Battle
Pay to reveal opponent weakness

The competitive relationship should remain understandable and fair.

20. Friends and Social Monetization

Friends are a competition network, not a social network.

Do not monetize through:

paid messages,
paid chat,
paid likes,
paid followers,
paid social boosts,
paid visibility.

Chat/social-network monetization is outside the current product scope.

21. Notifications and Monetization

Notifications must not become advertising spam.

If monetization notifications are introduced later, they require explicit:

permission model,
frequency,
opt-out behavior,
content rules,
privacy review.
22. Purchase UX Principles

Any future purchase flow must be:

explicit,
understandable,
reversible where applicable,
transparent about price,
transparent about billing,
transparent about renewal,
accessible,
non-deceptive.

Avoid:

hidden charges,
ambiguous CTA wording,
misleading countdowns,
fake scarcity,
confusing cancellation,
disguised purchase buttons.
23. Purchase Confirmation

A purchase must never happen from an accidental tap.

Purchase-related UI should provide:

Product
Price
Billing frequency
What is included
Renewal information where applicable
Restore purchase
Terms where applicable

Exact implementation depends on the eventual billing platform and monetization decision.

24. Entitlement Model

If premium functionality is eventually implemented, the technical model should distinguish:

User
    ↓
Entitlements
    ↓
Feature access

Do not scatter premium checks throughout UI code.

A centralized entitlement abstraction is preferred.

Example conceptual interface:

EntitlementService

This is an architectural proposal, not a locked implementation requirement.

25. Entitlement Security

Premium entitlement must not be trusted solely because the client says:

isPremium = true

Where server-side verification is required, entitlement validation should use the approved backend/store architecture.

Exact implementation is PENDING.

26. Offline Premium State

Offline behavior for premium entitlements must be explicitly defined before implementation.

Questions include:

Can premium functionality work offline?
How long may a cached entitlement remain valid?
What happens when entitlement verification fails?
Does the app fail open or fail closed?

These decisions are PENDING.

27. Restore Purchases

If purchases are introduced, the product should support appropriate restoration of valid purchases.

Exact behavior depends on the eventual billing architecture.

At minimum the UX should not require users to repurchase an already-owned entitlement merely because the app was reinstalled.

28. Subscription Cancellation

If subscription monetization is selected, cancellation should follow the applicable platform billing model.

The app should clearly communicate where users manage subscription status.

Exact UX is PENDING.

29. Pricing

No production price is currently locked.

Do not hardcode arbitrary prices into the application.

Pricing should be represented through the eventual billing/product configuration.

30. Regional Pricing

If Daily Battle launches across multiple markets, regional pricing may be relevant.

Exact supported countries, currencies, and pricing strategy are PENDING.

Do not assume India-only pricing or a single global price unless explicitly decided.

31. Taxes and Billing

Tax, billing, refund, and legal requirements depend on the final distribution and monetization model.

These must be reviewed before production monetization.

Antigravity must not invent legal/tax rules.

32. Monetization and Product Metrics

Monetization must not be optimized in isolation.

Relevant product metrics may eventually include:

Daily active users
Battle completion
Return rate
Practice usage
Friend interaction
Rival interaction
Premium conversion
Ad engagement
Revenue
Retention

Exact analytics events and KPIs belong to 14_ANALYTICS.md.

This document does not define their final event schema.

33. Monetization Metrics Must Not Override Product Integrity

A monetization experiment must not justify:

increasing artificial frustration,
degrading free gameplay,
interrupting official Battle,
manipulating score,
hiding important information,
creating fake scarcity,
increasing notification spam.

Revenue optimization is subordinate to the product's approved competitive and UX rules.

34. Monetization Experimentation

Future monetization experiments must be explicitly controlled.

Each experiment should define:

Experiment ID
Hypothesis
Target population
Variant
Control
Metric
Duration
Success criteria
Risk
Rollback condition
Decision owner

Antigravity must not independently launch experiments.

35. A/B Testing

A/B testing of monetization is PENDING.

If introduced, the experiment system must ensure:

deterministic assignment where required,
stable variant assignment,
correct analytics attribution,
no cross-contamination,
safe rollback.

Experiments must not alter Official Battle fairness.

36. Monetization State Model

If monetization is implemented, the product should conceptually support states such as:

FREE
PREMIUM
PURCHASE_PENDING
PURCHASE_SUCCESS
PURCHASE_FAILED
ENTITLEMENT_UNKNOWN

Exact state model is PENDING until a billing model is selected.

37. Purchase Failure Handling

A failed purchase must:

not grant entitlement incorrectly,
not corrupt user state,
not lock the user permanently,
provide understandable feedback,
allow appropriate retry.

Unknown billing state must not be treated as confirmed success.

38. Duplicate Purchase Protection

Repeated purchase interaction must be handled safely.

Test:

Tap purchase
→ tap repeatedly

Expected:

no accidental duplicate application-level purchase flow,
correct billing state handling,
correct entitlement reconciliation.

Exact platform billing behavior remains implementation-dependent.

39. Monetization Error States

Future monetization UI must support:

Loading
Available
Purchase pending
Purchase success
Purchase failure
Unavailable
Offline
Entitlement unknown
Restore success
Restore failure

Only relevant states should be displayed for the selected billing model.

40. Monetization Accessibility

All monetization UI must follow 11_ACCESSIBILITY.md.

Verify:

readable prices,
readable billing frequency,
accessible buttons,
clear selected state,
clear purchase state,
screen-reader semantics,
sufficient contrast,
no color-only purchase state,
dynamic text support.
41. Monetization Visual Rules

Monetization UI must remain consistent with the Daily Battle visual system.

Avoid turning the product into:

casino UI,
aggressive storefront UI,
flashing sales UI,
neon ad-heavy UI,
excessive promotional cards.

The overall product direction remains:

Premium interactive product with gaming energy.

42. Monetization Placement

The following locations should be considered separately:

Home
Results
Profile
History
Settings
Practice

No monetization placement is currently locked.

Gameplay screens should remain protected from intrusive monetization.

43. Home Monetization

The Home screen's primary purpose remains:

Get the user into Today's Battle.

Therefore monetization must not overpower:

TODAY'S BATTLE
PLAY BATTLE

The Daily Battle CTA remains the dominant action.

44. Results Monetization

Results are a high-emotion moment.

If monetization is eventually placed here, it must not obscure:

score,
percentile,
improvement,
breakdown,
rival result.

The user must understand their performance before any optional commercial action.

45. Profile / History Monetization

Profile and History may eventually contain premium expansion opportunities.

Examples for future evaluation:

Advanced performance analysis
Longer historical data
Additional insights

These remain PENDING.

46. Practice Monetization

Practice is the most obvious area for evaluating expanded paid functionality because it is separate from the official competitive result.

Possible future concepts:

More practice rounds
Additional practice challenge variants
Extended practice analytics
Personalized practice

These are not approved features.

47. No Monetization Dependency in Core Gameplay

Gameplay architecture must not depend on a monetization system existing.

The following should remain independently testable:

Snap
Shift
Crowd Call
Scoring
Results
Rival

If monetization is disabled, these systems should still function according to the free/core product specification.

48. Feature Flags

If future monetization is introduced incrementally, feature gating may be appropriate.

Conceptually:

MONETIZATION_ENABLED
PREMIUM_ENABLED
ADS_ENABLED
PRACTICE_PREMIUM_ENABLED

These are conceptual examples.

Exact flag architecture is PENDING.

Do not add unnecessary flags until a real monetization feature exists.

49. Development Environment

Development and test builds must not accidentally trigger real purchases or production monetization.

Use appropriate test/sandbox billing environments once the billing provider is selected.

Exact provider is PENDING.

50. Monetization QA

Any monetization implementation must be tested for:

Functional
purchase,
restore,
cancellation,
entitlement,
expiry where applicable,
failed payment,
retry.
Product
no pay-to-win,
no official score manipulation,
no unfair advantage.
UX
clear price,
clear value,
clear CTA,
correct placement.
Accessibility
semantic labels,
touch targets,
readable content.
Resilience
offline,
timeout,
interrupted purchase,
app restart,
process death.
Security
entitlement integrity,
no client-only trust where inappropriate,
no exposed secrets.
51. Monetization Regression Tests

Once monetization exists, every significant change must verify that:

Official Battle
    ↓
still fair

Practice
    ↓
still isolated

Results
    ↓
still readable

Home
    ↓
still battle-first

Monetization must not regress the core game loop.

52. Anti-Manipulation Rules

Daily Battle should not use manipulative patterns such as:

false countdowns,
fake limited availability,
intentionally misleading button hierarchy,
hidden cancellation,
preselected paid upgrades,
confusing free-vs-paid wording,
fake "loss" messaging designed solely to induce payment,
repeated purchase prompts after dismissal.

Any future monetization UX should be reviewed against these principles.

53. Permissions

Monetization must not introduce unnecessary permissions.

Examples:

Contacts permission
Location permission
Camera permission
Microphone permission

must not be added merely to support monetization.

54. Privacy

Monetization-related data collection must be explicitly documented.

Do not collect additional personal information simply because it could theoretically improve monetization.

Analytics and privacy details are governed by their respective specifications.

55. Third-Party Dependencies

Any billing, advertising, attribution, or monetization SDK is an external dependency.

Before adding one, Antigravity must report:

SDK name
Purpose
Version
Permissions
Data collected
Network behavior
License
Build impact
Privacy implications
Security implications

No monetization SDK may be added without authorization.

56. Monetization Architecture Boundary

The architecture should isolate monetization from gameplay.

Conceptually:

UI
 ↓
Presentation
 ↓
Monetization Use Cases
 ↓
Entitlement / Billing Abstraction
 ↓
Platform / Backend Integration

Gameplay should not directly depend on a billing SDK.

57. Recommended Conceptual Interfaces

These are architectural proposals only:

BillingRepository
EntitlementRepository
PurchaseUseCase
RestorePurchaseUseCase
GetEntitlementsUseCase

The exact names and architecture remain subject to 07_TECHNICAL_ARCHITECTURE.md.

58. No Premature Monetization Implementation

Until the commercial model is explicitly selected:

Antigravity must NOT:

add billing SDKs,
add ad SDKs,
create premium screens,
create subscription screens,
create purchase buttons,
create fake premium state,
create pricing,
create coin systems,
create entitlement infrastructure solely for future use.

The product should first establish a stable core experience.

59. Monetization Decision Process

Before implementing any monetization model:

Product decision
      ↓
Define monetization model
      ↓
Define user value
      ↓
Define entitlement
      ↓
Define UX
      ↓
Define data/API requirements
      ↓
Define legal/privacy requirements
      ↓
Define analytics
      ↓
Define QA
      ↓
Approve
      ↓
Implement
60. Monetization Requirement IDs

Future requirements should use:

REQ-MON-001
REQ-MON-002
REQ-MON-003
...

Example:

REQ-MON-001
Core Official Battle remains accessible without payment.

REQ-MON-002
Paid functionality must not directly increase Official Battle score.

REQ-MON-003
No generalized coin economy exists unless explicitly approved.

REQ-MON-004
Monetization must not interrupt active gameplay unless explicitly approved.

REQ-MON-005
Premium entitlements must be separated from gameplay scoring logic.
61. Current Locked Monetization Requirements
REQ-MON-001

The core Daily Battle experience must remain meaningful without payment.

REQ-MON-002

Monetization must not directly provide Official Battle competitive advantage.

REQ-MON-003

No generalized coin/currency economy is part of the current product.

REQ-MON-004

No pay-to-win mechanics.

REQ-MON-005

Monetization must not compromise Official Battle integrity.

REQ-MON-006

Practice and Official Battle remain separate.

REQ-MON-007

No monetization feature may be implemented merely because it is commercially common.

REQ-MON-008

Any future monetization feature requires explicit product approval.

REQ-MON-009

Monetization implementation must not introduce unauthorized permissions, data collection, or third-party SDKs.

REQ-MON-010

Monetization must remain subordinate to the core Daily Battle UX.

62. Current Monetization Pending Decisions

The following decisions must remain explicitly unresolved:

MON-PENDING-001

Does Daily Battle use ads?

MON-PENDING-002

If ads exist, which formats are permitted?

MON-PENDING-003

Does Daily Battle use a subscription?

MON-PENDING-004

Does Daily Battle use a one-time premium purchase?

MON-PENDING-005

Are cosmetics monetized?

MON-PENDING-006

Are advanced personal insights monetized?

MON-PENDING-007

Is expanded Practice monetized?

MON-PENDING-008

Does premium remove advertising?

MON-PENDING-009

What are the final premium entitlements?

MON-PENDING-010

What are the production prices?

MON-PENDING-011

What markets/currencies are supported?

MON-PENDING-012

What billing platform/provider is used?

MON-PENDING-013

Is server-side entitlement verification required?

MON-PENDING-014

What is the offline entitlement policy?

MON-PENDING-015

What purchase/restore UX is required?

MON-PENDING-016

What monetization analytics events are required?

MON-PENDING-017

What experiments, if any, are permitted?

MON-PENDING-018

What legal/privacy review is required before production monetization?

63. Monetization Definition of Done

A monetization feature is DONE only when:

Product
 Monetization model explicitly approved
 User value explicitly defined
 Entitlement explicitly defined
 Free/core experience remains valid
 Official Battle remains fair
UX
 Purchase flow is understandable
 Pricing is clear
 Entitlements are clear
 Cancellation/restore behavior is clear where applicable
 No manipulative interaction patterns
 UI follows Daily Battle design system
Technical
 Billing abstraction implemented
 Entitlement handling implemented
 Error handling implemented
 Offline behavior defined
 Duplicate purchase handling tested
 Lifecycle behavior tested
 Security reviewed
 Dependencies approved
QA
 Purchase success tested
 Purchase failure tested
 Restore tested
 Offline tested
 Process death tested
 Accessibility tested
 Regression tested
 Official Battle fairness verified
 Evidence collected
Governance
 Requirements traced
 Analytics defined
 Privacy implications reviewed
 Deviations recorded
 Product approval recorded
64. Antigravity Monetization Rules

Antigravity must:

Treat monetization as an explicitly controlled product subsystem.
Never invent a monetization model.
Never add ads without approval.
Never add billing without approval.
Never add coins/currency.
Never add pay-to-win mechanics.
Never allow purchases to modify Official Battle scores.
Never make Practice alter Official Battle results.
Never add a monetization SDK without approval.
Never hardcode production pricing without an approved pricing specification.
Never create fake premium functionality merely for architecture preparation.
Report all monetization dependencies.
Report all permissions introduced by monetization.
Report all data collection introduced by monetization.
Keep monetization isolated from gameplay logic.
Stop when a monetization decision is unresolved.
Ask for a product decision instead of guessing.
65. Final Monetization Principle

The product hierarchy remains:

PLAY
 ↓
PERFORM
 ↓
SEE SCORE
 ↓
COMPARE
 ↓
IMPROVE
 ↓
RETURN TOMORROW

Monetization must support this loop.

It must never replace it.

Daily Battle should earn revenue from providing additional value — not from weakening the core Battle experience.
