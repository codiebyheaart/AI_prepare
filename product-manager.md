# 👔 Product Manager, Team Lead & Agile — Practical Guide

> **For:** Senior Developer moving toward Tech Lead / Architect / PM-adjacent roles (2026–2027)

---

## 1. Why Senior Developers Need PM & Agile Knowledge

At the 8+ year level, you're not just coding. You're:
- **Estimating** and planning sprints
- **Communicating** with stakeholders and business teams
- **Mentoring** junior developers
- **Making architecture decisions** that affect the product roadmap
- **Translating** business requirements into technical solutions

Interviewers expect you to understand the **business context** of your technical work.

---

## 2. Product Manager (PM) — What They Do

A Product Manager is the **"CEO of the product"** — they own the vision, roadmap, and success metrics of a product.

### PM's Core Responsibilities

| Responsibility | What it means |
|---------------|--------------|
| **Discovery** | Talk to users, find real problems worth solving |
| **Roadmap** | Decide what gets built and in what order |
| **Prioritization** | Choose between 50 ideas — what to do NOW |
| **Requirements** | Write user stories, acceptance criteria |
| **Stakeholder management** | Align business, engineering, design, sales |
| **Metrics** | Define success — DAU, conversion rate, NPS |
| **Go-to-market** | Coordinate launch with marketing/sales |

### What a Good Developer Knows About PM

- **User Story Format:** "As a [user], I want to [action] so that [benefit]"
- **Acceptance Criteria:** Clear, testable conditions that define "done"
- **MoSCoW Prioritization:** Must Have / Should Have / Could Have / Won't Have
- **OKRs:** Objective and Key Results — how the company measures success
- **KPIs:** Key Performance Indicators — metrics that show progress

---

## 3. Core Product Frameworks

### 3.1 User Story Mapping
Visualize the user journey and map features to each step.

```
User Journey: [Discover] → [Sign Up] → [Fund Account] → [Trade] → [Track Portfolio]

Features mapped to each step:
Discover:     Landing page, SEO, referral program
Sign Up:      OTP verification, KYC, email confirmation
Fund Account: UPI, bank transfer, NEFT/RTGS
Trade:        Order placement, order book, watchlist
Track:        Portfolio view, P&L, alerts
```

### 3.2 RICE Scoring (Prioritization)
RICE = Reach × Impact × Confidence ÷ Effort

| Feature | Reach | Impact | Confidence | Effort | Score |
|---------|-------|--------|------------|--------|-------|
| Real-time alerts | 5000 | 3 | 80% | 2 weeks | 600 |
| Dark mode | 2000 | 1 | 90% | 1 week | 180 |
| AI recommendations | 1000 | 5 | 60% | 6 weeks | 50 |

→ Build in order: Alerts → Dark Mode → AI (after more validation)

### 3.3 Product Vision Board
| Element | Example |
|---------|---------|
| **Target Group** | Retail investors aged 25–45 in India |
| **Needs** | Simple, intelligent way to manage stock portfolio |
| **Product** | AI-powered trading assistant app |
| **Business Goals** | 1M users in 18 months, ₹50Cr ARR |
| **Value** | Save 2 hrs/week of manual research per user |

---

## 4. Agile Methodology — In Depth

Agile is an **iterative, collaborative approach** to software development. Work is done in short cycles (sprints) with continuous feedback.

### Agile vs Waterfall

| Aspect | Waterfall | Agile |
|--------|-----------|-------|
| **Planning** | Plan everything upfront | Plan sprint by sprint |
| **Delivery** | Ship at the end (months) | Ship every 2 weeks |
| **Changes** | Hard to accommodate | Embraced and expected |
| **Feedback** | After full delivery | Continuous |
| **Risk** | High (find problems late) | Low (fail fast, adjust early) |

---

## 5. Scrum — The Most Common Agile Framework

### Scrum Roles

| Role | Responsibility |
|------|---------------|
| **Product Owner (PO)** | Owns the backlog, prioritizes features |
| **Scrum Master (SM)** | Facilitates the process, removes blockers |
| **Development Team** | Self-organized, delivers the work |

### Scrum Events (Ceremonies)

| Event | Duration | Purpose |
|-------|---------|---------|
| **Sprint Planning** | 2-4 hours | Decide what to build this sprint |
| **Daily Standup** | 15 minutes | Sync the team (What did I do? What will I do? Blockers?) |
| **Sprint Review** | 1-2 hours | Demo completed work to stakeholders |
| **Sprint Retrospective** | 1-2 hours | What went well? What to improve? |
| **Backlog Refinement** | 1-2 hours | Clarify and estimate upcoming stories |

### Sprint Cycle (2 Weeks)
```
[Sprint Planning] → [Week 1 Development] → [Week 2 Development]
                                    ↓
                          [Sprint Review + Demo]
                                    ↓
                          [Sprint Retrospective]
                                    ↓
                          [Next Sprint Starts]
```

---

## 6. Kanban — Continuous Flow Alternative

Kanban is a **visual workflow system** — no fixed sprints, work flows continuously.

```
[Backlog] → [To Do] → [In Progress] → [Code Review] → [Testing] → [Done]
               3          2 (WIP limit)      2              1
```

**WIP Limit (Work In Progress):** Cap on items in each column — prevents overloading.

### When to Use Kanban vs Scrum

| Scenario | Use |
|---------|-----|
| Support team with random incoming work | Kanban |
| Feature development with predictable scope | Scrum |
| DevOps / continuous deployment | Kanban |
| Product development with planning | Scrum |

---

## 7. Estimation Techniques

### Story Points (Fibonacci Scale)
1, 2, 3, 5, 8, 13, 21 — represents **relative complexity**, not hours.

```
1 point  = Trivial change (typo fix, config update)
3 points = Small feature (add an API endpoint)
5 points = Medium feature (with DB changes)
8 points = Complex feature (integration with external system)
13 points = Large — should be broken down further
```

### Planning Poker
Team votes simultaneously on story size. Discuss when there's a big difference. Prevents anchoring bias.

### T-Shirt Sizing (Quick Estimation)
XS / S / M / L / XL — for early-stage roadmap estimation when you don't have details yet.

---

## 8. Team Leadership for Senior Developers

### What Technical Leadership Looks Like

| Skill | How to Demonstrate |
|-------|-------------------|
| **Mentoring** | Pair programming, code reviews with explanations |
| **Decision Making** | Architecture ADRs (Architecture Decision Records) |
| **Communication** | Translate tech to business language in meetings |
| **Conflict Resolution** | Facilitate design disagreements constructively |
| **Ownership** | Take responsibility for team deliverables |
| **Delegation** | Assign work to match team members' growth goals |

### Running Effective Code Reviews
- Be kind, review the code not the person
- Ask questions ("What would happen if...?") vs making demands
- Approve what's good, suggest improvements as non-blocking
- Share knowledge: "Here's a pattern that could simplify this..."
- Agree on standards upfront (linting, formatting) to reduce nitpicks

---

## 9. Communication Patterns for Tech Leads

### The "Pyramid Principle" for Updates
Lead with the conclusion, then support with details.

```
❌ "We explored 3 options for the caching layer and tested Redis vs Memcached
    and measured latency in various scenarios and ultimately..."

✅ "Recommendation: Use Redis for caching.
    Reason: 40% lower latency than Memcached in our load tests, plus it
    supports persistence and pub/sub which we'll need for Phase 2."
```

### Status Updates (Daily/Weekly)
```
What's Done:    Completed user authentication API + unit tests
What's Next:    Integrating with payment gateway tomorrow
Blockers:       Need API credentials from payment team (following up with PM)
Risk:           Payment integration may take 2 extra days if docs are incomplete
```

---

## 10. OKRs & Metrics — What Every Senior Dev Should Know

### OKRs (Objectives and Key Results)
Used by Google, Netflix, Spotify — aligns team goals with company goals.

**Example — Trading Platform:**
```
Objective: Make our AI features industry-leading by Q4 2026

Key Results:
  KR1: AI response time < 500ms for 95% of queries
  KR2: 40% of users engage with AI recommendations weekly
  KR3: AI-driven trades show 15% better returns vs non-AI
```

### Common Product Metrics in Finance Apps

| Metric | Meaning |
|--------|---------|
| **DAU/MAU** | Daily/Monthly Active Users |
| **Retention Rate** | % users who come back (Day 7, Day 30) |
| **Churn Rate** | % users who leave |
| **ARPU** | Average Revenue Per User |
| **NPS** | Net Promoter Score (how likely to recommend) |
| **Conversion Rate** | % who complete key action (signup → first trade) |
| **Latency (p50/p95/p99)** | Response time at different percentiles |
| **Error Rate** | % of requests that fail |
| **MTTR** | Mean Time to Recovery (after incidents) |

---

## 11. Agile Ceremonies as a Senior Developer

### Your Role in Sprint Planning
- Break down large stories into tasks (max 1 day each)
- Identify technical dependencies early
- Flag capacity risks: "This 5-point story needs infra setup first"
- Help less experienced devs estimate their tasks

### Your Role in Retrospectives
**What went well:**
- "Our pair programming on the payment integration caught 3 bugs early"

**What to improve:**
- "We should define API contracts before implementation starts — caught a mismatch on day 8 of the sprint"

**Action items:**
- "I'll create an API contract template we use from next sprint"

### Your Role in Daily Standup
Keep it under 60 seconds:
- "Yesterday I finished the RAG pipeline integration and it's in review"
- "Today I'm working on the vector DB connection pool issue"
- "No blockers — but I need 30 min with Raj about the embeddings format"

---

## 12. Interview Questions on PM/Agile

### Q: "How do you handle disagreements with a PM on priorities?"
> "I first make sure I understand their reasoning — sometimes the business context changes priority. Then I present technical risks with data: 'Skipping this refactor means our next 3 features will each take 40% longer.' I document it as a risk in the sprint. If it's a safety/reliability issue, I escalate clearly. Ultimately, I align with the decision while making my concerns visible."

### Q: "How do you manage a team when deadlines are tight?"
> "I start by assessing what can be cut without breaking core value — often it's polish, not features. I communicate early with stakeholders about scope vs timeline trade-offs. I protect the team from context-switching and ensure everyone has clear priorities. If we're still at risk, I escalate early — surprises on release day are the worst outcome."

### Q: "How do you mentor junior developers?"
> "I believe in teaching through doing — pair programming on real tasks, not toy exercises. In code reviews I explain the why behind suggestions. I ask juniors to design a solution first, then we discuss it — this builds their thinking muscle. I also share articles and encourage them to lead small parts of the sprint."

---

## Summary

| Domain | Key Takeaways |
|--------|--------------|
| **Product Management** | User stories, RICE prioritization, OKRs, metrics |
| **Scrum** | Sprint ceremonies, roles (PO/SM/Dev), velocity |
| **Kanban** | Visual flow, WIP limits, continuous delivery |
| **Estimation** | Story points, planning poker, T-shirt sizes |
| **Tech Leadership** | Mentoring, ADRs, code reviews, communication |
| **Metrics** | DAU, retention, MTTR, error rate, latency |

> 💡 **Interview Gold:** "At the senior level, my job isn't just to write code — it's to ensure the right things get built correctly. I partner with PMs on discovery, lead technical planning, mentor the team, and own quality end-to-end. Code is just one output."
