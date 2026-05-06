-- V3: Seed demo user and stock metadata

-- Demo user: demo@finsight.ai / password: Demo@1234
-- BCrypt hash of "Demo@1234"
INSERT INTO users (email, password_hash, full_name, role)
VALUES ('demo@finsight.ai',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkDYCLadyDq',
        'Rahul Sharma', 'TRADER')
ON CONFLICT (email) DO NOTHING;

-- Seed stock metadata (descriptions used for RAG & semantic search)
INSERT INTO stock_metadata (symbol, company_name, sector, industry, market_cap, description)
VALUES
  ('RELIANCE',  'Reliance Industries Ltd',     'Energy',          'Oil & Gas',         'Large Cap',  'Reliance Industries is India''s largest company by market cap, operating across oil refining, petrochemicals, retail (JioMart), and telecom (Jio). It has a diversified business model with strong cash flows and is investing heavily in green energy and digital services.'),
  ('TCS',       'Tata Consultancy Services',   'Technology',      'IT Services',       'Large Cap',  'TCS is India''s largest IT services company, serving clients across banking, finance, retail, and manufacturing. Known for strong margins (25%+), consistent dividend payouts, and a massive global workforce of 600,000+ employees.'),
  ('INFY',      'Infosys Ltd',                 'Technology',      'IT Services',       'Large Cap',  'Infosys is a leading global IT services company known for digital transformation, AI, and cloud services. Strong revenue growth in North America and Europe, with a focus on automation and operational efficiency.'),
  ('HDFCBANK',  'HDFC Bank Ltd',               'Finance',         'Banking',           'Large Cap',  'HDFC Bank is India''s largest private sector bank with a stellar track record of consistent earnings growth, low NPAs, and strong CASA ratios. Merger with HDFC Ltd completed, creating a financial powerhouse.'),
  ('ICICIBANK', 'ICICI Bank Ltd',              'Finance',         'Banking',           'Large Cap',  'ICICI Bank has transformed from a stressed asset-heavy bank to a high-growth, quality-focused institution. Strong digital banking platform (iMobile), improving ROE and asset quality, well-positioned for India''s credit growth.'),
  ('SBIN',      'State Bank of India',         'Finance',         'Banking',           'Large Cap',  'SBI is India''s largest public sector bank with unmatched branch network and government backing. Strong in retail and MSME lending with improving digital infrastructure and declining NPAs.'),
  ('BAJFINANCE','Bajaj Finance Ltd',            'Finance',         'NBFC',              'Large Cap',  'Bajaj Finance is India''s most valuable NBFC, known for its consumer lending, EMI cards, and digital payments. High ROE of 22%+, strong cross-selling, but exposed to rising interest rate risk.'),
  ('WIPRO',     'Wipro Ltd',                   'Technology',      'IT Services',       'Large Cap',  'Wipro is a global IT, consulting and BPO company focusing on BFSI, healthcare and energy verticals. Has been investing in acquisitions and AI capabilities to drive digital revenue growth.'),
  ('ASIANPAINT','Asian Paints Ltd',            'Consumer',        'Paints',            'Large Cap',  'Asian Paints is India''s largest paints company with 55%+ market share. Strong brand moat, rural penetration, and premium product portfolio. Benefiting from real estate cycle and renovation demand.'),
  ('MARUTI',    'Maruti Suzuki India Ltd',     'Auto',            'Passenger Vehicles', 'Large Cap', 'Maruti Suzuki dominates India''s passenger car market with 42%+ market share. Strong rural sales, growing SUV portfolio (Brezza, Ertiga, Grand Vitara), and improving margins as supply chain normalizes.'),
  ('TATAMOTORS','Tata Motors Ltd',             'Auto',            'Commercial Vehicles','Large Cap', 'Tata Motors is a global automotive company with brands including Jaguar Land Rover (JLR). Strong EV push in India (Nexon EV, Tigor EV) and JLR recovery driving significant profit growth.'),
  ('SUNPHARMA', 'Sun Pharmaceutical Ind Ltd',  'Healthcare',      'Pharmaceuticals',   'Large Cap',  'Sun Pharma is India''s largest pharma company, strong in specialty generics and API. US generics business stabilizing, specialty portfolio (Cequa, Ilumya) growing, and domestic formulations remain robust.'),
  ('DRREDDY',   'Dr Reddy''s Laboratories',   'Healthcare',      'Pharmaceuticals',   'Large Cap',  'Dr Reddy''s is a global pharma company with a strong US generics business, biosimilars pipeline, and branded formulations in India. Consistent EBITDA margins and growing free cash flow.'),
  ('ONGC',      'Oil and Natural Gas Corp',    'Energy',          'Oil & Gas',         'Large Cap',  'ONGC is India''s largest state-owned oil and gas explorer with significant upstream assets. Dividend play with high yield, though sensitive to crude oil prices and subsidy burden.'),
  ('LTIM',      'LTIMindtree Ltd',             'Technology',      'IT Services',       'Large Cap',  'LTIMindtree is a tier-1 IT company formed by merger of LTI and Mindtree, strong in BFSI and hi-tech verticals. Gaining deal momentum with cross-selling synergies and AI-led offerings.'),
  ('ADANIENT',  'Adani Enterprises Ltd',       'Conglomerate',    'Infrastructure',    'Large Cap',  'Adani Enterprises is the flagship of the Adani Group, incubating new businesses in airports, green hydrogen, data centers, and roads. High growth potential but elevated leverage and regulatory risk.'),
  ('HINDUNILVR','Hindustan Unilever Ltd',      'Consumer',        'FMCG',              'Large Cap',  'HUL is India''s leading FMCG company with iconic brands in home care, beauty, and foods. Pricing power, rural recovery, and premiumisation drive consistent double-digit earnings growth.'),
  ('KOTAKBANK', 'Kotak Mahindra Bank',         'Finance',         'Banking',           'Large Cap',  'Kotak Bank is a premium private sector bank known for conservative lending, high NIMs, and strong ROE. Growing retail franchise and wealth management business make it a quality compounder.'),
  ('TITAN',     'Titan Company Ltd',           'Consumer',        'Jewellery & Watches','Large Cap', 'Titan is India''s leading branded lifestyle company across jewellery (Tanishq), watches (Titan), and eyewear (Titan EyePlus). Strong brand equity, omnichannel retail expansion, and premiumisation drive consistent growth.'),
  ('POWERGRID', 'Power Grid Corp of India',    'Utilities',       'Power Transmission', 'Large Cap', 'Power Grid is a state-owned power transmission company with regulated return business model. Stable and predictable cash flows, high dividend yield, and growing capex in renewable energy integration.')
ON CONFLICT (symbol) DO NOTHING;
