/* global Html5Qrcode, Html5QrcodeScanner */
(function () {
	"use strict";

	const qs = (sel) => document.querySelector(sel);
	const qsa = (sel) => Array.from(document.querySelectorAll(sel));

	const state = {
		html5Qr: null,
		lastQrText: null,
		lastScanData: null,
		role: null, // 'customer' or 'pump'
		customerToken: null,
		pumpToken: null,
		customerApiBase: null,
		pumpApiBase: null
	};

	function setStatus(id, text, isError = false) {
		const el = qs(id);
		if (!el) return;
		el.textContent = text || "";
		el.classList.toggle("error", !!isError);
	}

	function showRoleSelection() {
		qs("#customerInterface").style.display = "none";
		qs("#pumpInterface").style.display = "none";
		setStatus("#roleInfo", "");
	}

	function showCustomerInterface() {
		state.role = "customer";
		qs("#customerInterface").style.display = "block";
		qs("#pumpInterface").style.display = "none";
		setStatus("#roleInfo", "Customer mode activated");
		loadCustomerConfig();
		initCustomerTabs();
		showCustomerPage("dashboard");
	}

	function showPumpInterface() {
		state.role = "pump";
		qs("#customerInterface").style.display = "none";
		qs("#pumpInterface").style.display = "block";
		setStatus("#roleInfo", "Pump operator mode activated");
		loadPumpConfig();
		initPumpTabs();
		showPumpPage("dashboard");
	}

	function initCustomerTabs() {
		qsa('[data-c-tab]').forEach(btn => {
			btn.addEventListener('click', () => {
				const tab = btn.getAttribute('data-c-tab');
				window.location.hash = `#c/${tab}`;
			});
		});
	}

	function initPumpTabs() {
		qsa('[data-p-tab]').forEach(btn => {
			btn.addEventListener('click', () => {
				const tab = btn.getAttribute('data-p-tab');
				window.location.hash = `#p/${tab}`;
			});
		});
	}

	function activateTabs(scope, tab) {
		const selector = scope === 'c' ? '[data-c-tab]' : '[data-p-tab]';
		qsa(selector).forEach(btn => {
			if (btn.getAttribute(selector.replace('[','').replace(']','').split('=')[0]) === tab) {
				btn.classList.add('active');
			} else {
				btn.classList.remove('active');
			}
		});
	}

	function showCustomerPage(page) {
		qsa('#customerInterface .page').forEach(el => el.style.display = 'none');
		const el = qs(`#c-${page}`);
		if (el) el.style.display = 'block';
		activateTabs('c', page);
		if (page === 'dashboard') customerLoadDashboard();
		if (page === 'transactions') customerLoadTransactions(1);
		if (page === 'wallet') walletGetBalance();
	}

	function showPumpPage(page) {
		qsa('#pumpInterface .page').forEach(el => el.style.display = 'none');
		const el = qs(`#p-${page}`);
		if (el) el.style.display = 'block';
		activateTabs('p', page);
	}

	window.addEventListener('hashchange', () => {
		const hash = window.location.hash || '';
		if (hash.startsWith('#c/')) {
			const page = hash.slice(3);
			showCustomerPage(page);
		} else if (hash.startsWith('#p/')) {
			const page = hash.slice(3);
			showPumpPage(page);
		}
	});

	// Customer functions
	function loadCustomerConfig() {
		const saved = localStorage.getItem("zappay.customer.config");
		if (saved) {
			try {
				const config = JSON.parse(saved);
				state.customerApiBase = config.apiBase;
				qs("#customerApiBase").value = state.customerApiBase;
				const tokens = localStorage.getItem("zappay.customer.tokens");
				if (tokens) {
					const parsed = JSON.parse(tokens);
					state.customerToken = parsed.access;
					setStatus("#customerAuthStatus", "Session restored");
				}
			} catch (e) {
				console.error("Failed to load customer config:", e);
			}
		} else {
			state.customerApiBase = window.location.origin || "http://localhost:8000";
			qs("#customerApiBase").value = state.customerApiBase;
		}
	}

	function saveCustomerConfig() {
		const base = (qs("#customerApiBase").value || "").trim();
		if (!base) {
			setStatus("#customerConfigStatus", "Please provide API Base URL", true);
			return;
		}
		state.customerApiBase = base.replace(/\/+$/, "");
		localStorage.setItem("zappay.customer.config", JSON.stringify({ apiBase: state.customerApiBase }));
		setStatus("#customerConfigStatus", `Saved: ${state.customerApiBase}`);
	}

	async function customerLogin() {
		try {
			setStatus("#customerAuthStatus", "Logging in…");
			const phone = (qs("#customerPhone").value || "").trim();
			const password = (qs("#customerPassword").value || "").trim();
			if (!phone || !password) {
				setStatus("#customerAuthStatus", "Phone and password required", true);
				return;
			}
			const url = `${state.customerApiBase}/api/v1/auth/login`;
			const body = new URLSearchParams({ username: phone, password });
			const res = await fetch(url, {
				method: "POST",
				headers: { "Content-Type": "application/x-www-form-urlencoded" },
				body
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Login failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			state.customerToken = data.access_token;
			localStorage.setItem("zappay.customer.tokens", JSON.stringify({ access: state.customerToken, refresh: data.refresh_token }));
			setStatus("#customerAuthStatus", "Logged in as customer");
		} catch (e) {
			console.error(e);
			setStatus("#customerAuthStatus", e.message || "Login error", true);
		}
	}

	function customerLogout() {
		state.customerToken = null;
		localStorage.removeItem("zappay.customer.tokens");
		setStatus("#customerAuthStatus", "Logged out");
	}

	function customerAuthHeaders() {
		return state.customerToken ? { Authorization: `Bearer ${state.customerToken}` } : {};
	}

	async function customerSignupSendOtp() {
		try {
			setStatus("#customerSignupStatus", "Sending OTP…");
			const full_name = (qs("#signupFullName").value || "").trim();
			const phone_number = (qs("#signupPhone").value || "").trim();
			if (!full_name || !phone_number) {
				setStatus("#customerSignupStatus", "Name and phone required", true);
				return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/auth/register/otp/start`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ full_name, phone_number, role: "customer" })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Send OTP failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			if (data.otp_debug) {
				setStatus("#customerSignupStatus", `OTP sent. Debug: ${data.otp_debug}`);
			} else {
				setStatus("#customerSignupStatus", "OTP sent. Check server logs.");
			}
		} catch (e) {
			console.error(e);
			setStatus("#customerSignupStatus", e.message || "Error", true);
		}
	}

	async function customerSignupComplete() {
		try {
			setStatus("#customerSignupStatus", "Completing signup…");
			const full_name = (qs("#signupFullName").value || "").trim();
			const phone_number = (qs("#signupPhone").value || "").trim();
			const password = (qs("#signupPassword").value || "").trim();
			const otp_code = (qs("#signupOtpCode").value || "").trim();
			if (!full_name || !phone_number || !password || !otp_code) {
				setStatus("#customerSignupStatus", "All fields required", true);
			 return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/auth/register/otp/complete`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ full_name, phone_number, password, otp_code, role: "customer" })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Signup failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			state.customerToken = data.access_token;
			localStorage.setItem("zappay.customer.tokens", JSON.stringify({ access: state.customerToken, refresh: data.refresh_token }));
			setStatus("#customerSignupStatus", "Signup complete & logged in");
		} catch (e) {
			console.error(e);
			setStatus("#customerSignupStatus", e.message || "Error", true);
		}
	}

	async function customerSendOtpLogin() {
		try {
			setStatus("#customerAuthStatus", "Sending OTP…");
			const phone_number = (qs("#customerPhone").value || "").trim();
			if (!phone_number) {
				setStatus("#customerAuthStatus", "Phone required", true);
				return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/auth/send-otp`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ phone_number, otp_type: "login" })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Send OTP failed: ${res.status} ${t}`);
			}
			setStatus("#customerAuthStatus", "OTP sent");
		} catch (e) {
			console.error(e);
			setStatus("#customerAuthStatus", e.message || "Error", true);
		}
	}

	async function customerOtpLogin() {
		try {
			setStatus("#customerAuthStatus", "Verifying OTP…");
			const phone_number = (qs("#customerPhone").value || "").trim();
			const otp_code = (qs("#customerOtpCode").value || "").trim();
			if (!phone_number || !otp_code) {
				setStatus("#customerAuthStatus", "Phone and OTP required", true);
				return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/auth/login/otp`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ phone_number, otp_code, otp_type: "login" })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`OTP login failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			state.customerToken = data.access_token;
			localStorage.setItem("zappay.customer.tokens", JSON.stringify({ access: state.customerToken, refresh: data.refresh_token }));
			setStatus("#customerAuthStatus", "Logged in via OTP");
		} catch (e) {
			console.error(e);
			setStatus("#customerAuthStatus", e.message || "Error", true);
		}
	}

	async function generateQr() {
		try {
			setStatus("#qrStatus", "Generating…");
			qs("#qrImageContainer").innerHTML = "";
			const qrType = qs("#qrType").value || "mobile";
			const validityHours = (qs("#validityHours").value || "").trim();
			const payload = { qr_type: qrType };
			if (validityHours) payload.validity_hours = Number(validityHours);

			const res = await fetch(`${state.customerApiBase}/api/v1/qr/generate`, {
				method: "POST",
				headers: { "Content-Type": "application/json", ...customerAuthHeaders() },
				body: JSON.stringify(payload)
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Generate failed: ${res.status} ${t}`);
			}
			const qr = await res.json();
			const imgRes = await fetch(`${state.customerApiBase}/api/v1/qr/${qr.id}/image`, {
				headers: { ...customerAuthHeaders() }
			});
			if (!imgRes.ok) {
				const t = await imgRes.text();
				throw new Error(`Image fetch failed: ${imgRes.status} ${t}`);
			}
			const imgData = await imgRes.json();
			const img = document.createElement("img");
			img.alt = "QR Code";
			img.src = `data:image/png;base64,${imgData.image_data}`;
			qs("#qrImageContainer").appendChild(img);
			setStatus("#qrStatus", "QR generated - ready to scan at pump!");
		} catch (e) {
			console.error(e);
			setStatus("#qrStatus", e.message || "Generate error", true);
		}
	}

	async function customerLoadDashboard() {
		try {
			const res = await fetch(`${state.customerApiBase}/api/v1/users/dashboard`, {
				headers: { ...customerAuthHeaders() }
			});
			if (!res.ok) throw new Error(await res.text());
			const data = await res.json();
			const s = qs('#c-dashboard-summary');
			s.innerHTML = `
				<div class="kv"><span>Wallet Balance</span><span>₹ ${Number(data.wallet_balance || 0).toFixed(2)}</span></div>
				<div class="kv"><span>Total Transactions</span><span>${data.total_transactions || 0}</span></div>
				<div class="kv"><span>Total Spent</span><span>₹ ${Number(data.total_spent || 0).toFixed(2)}</span></div>
			`;
			const r = qs('#c-dashboard-recent');
			r.innerHTML = (data.recent_transactions || []).map(tx =>
				`<div class="kv"><span>${tx.transaction_id} (${tx.transaction_type})</span><span>₹ ${Number(tx.amount || 0).toFixed(2)}</span></div>`
			).join('') || '<p class="muted">No recent transactions</p>';
		} catch (e) {
			console.error(e);
		}
	}

	let cTransPage = 1;
	async function customerLoadTransactions(page) {
		try {
			cTransPage = page;
			const res = await fetch(`${state.customerApiBase}/api/v1/transactions/history?page=${page}&page_size=10`, {
				headers: { ...customerAuthHeaders() }
			});
			if (!res.ok) throw new Error(await res.text());
			const data = await res.json();
			const list = qs('#c-transactions-list');
			list.innerHTML = (data.transactions || []).map(tx =>
				`<div class="kv"><span>${tx.transaction_id} (${tx.transaction_type})</span><span>₹ ${Number(tx.amount || 0).toFixed(2)}</span></div>`
			).join('') || '<p class="muted">No transactions</p>';
			qs('#c-trans-page').textContent = `Page ${data.page} / ${data.total_pages}`;
		} catch (e) {
			console.error(e);
		}
	}

	async function getProfile() {
		try {
			setStatus("#profileStatus", "Loading…");
			const res = await fetch(`${state.customerApiBase}/api/v1/users/profile`, {
				headers: { ...customerAuthHeaders() }
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Profile fetch failed: ${res.status} ${t}`);
			}
			const profile = await res.json();
			renderProfile(profile);
			setStatus("#profileStatus", "Profile loaded");
		} catch (e) {
			console.error(e);
			setStatus("#profileStatus", e.message || "Profile error", true);
		}
	}

	function renderProfile(profile) {
		const el = qs("#profileData");
		el.innerHTML = `
			<div class="kv"><span>Name</span><span>${escapeHtml(profile.full_name || "")}</span></div>
			<div class="kv"><span>Phone</span><span>${escapeHtml(profile.phone_number || "")}</span></div>
			<div class="kv"><span>Email</span><span>${escapeHtml(profile.email || "")}</span></div>
			<div class="kv"><span>Role</span><span>${escapeHtml(profile.role || "")}</span></div>
			<div class="kv"><span>Status</span><span>${profile.is_active ? "Active" : "Inactive"}</span></div>
		`;
	}

	async function walletGetBalance() {
		try {
			const res = await fetch(`${state.customerApiBase}/api/v1/wallet/balance`, {
				headers: { ...customerAuthHeaders() }
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Balance fetch failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			qs("#walletBalance").textContent = `Wallet #${data.id} — ₹ ${Number(data.balance || 0).toFixed(2)}`;
		} catch (e) {
			console.error(e);
			setStatus("#walletStatus", e.message || "Error", true);
		}
	}

	async function walletTestRecharge() {
		try {
			setStatus("#walletStatus", "Recharging…");
			const amount = Number(qs("#rechargeAmount").value || 0);
			if (!amount || amount <= 0) {
				setStatus("#walletStatus", "Enter valid amount", true);
				return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/wallet/test-recharge?amount=${amount}`, {
				method: "POST",
				headers: { ...customerAuthHeaders() }
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Recharge failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			setStatus("#walletStatus", `Recharged ₹${amount}. New balance: ₹${Number(data.data.new_balance || 0).toFixed(2)}`);
			walletGetBalance();
		} catch (e) {
			console.error(e);
			setStatus("#walletStatus", e.message || "Error", true);
		}
	}

	async function submitKyc() {
		try {
			setStatus("#kycStatus", "Submitting…");
			const aadhaar_number = (qs("#aadhaar").value || "").trim();
			const pan_number = (qs("#pan").value || "").trim();
			const driving_license = (qs("#dl").value || "").trim();
			if (!aadhaar_number || !pan_number) {
				setStatus("#kycStatus", "Aadhaar and PAN required", true);
				return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/users/kyc/submit`, {
				method: "POST",
				headers: { "Content-Type": "application/json", ...customerAuthHeaders() },
				body: JSON.stringify({ aadhaar_number, pan_number, driving_license })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`KYC submit failed: ${res.status} ${t}`);
			}
			setStatus("#kycStatus", "KYC submitted. Verification in progress.");
		} catch (e) {
			console.error(e);
			setStatus("#kycStatus", e.message || "Error", true);
		}
	}

	async function getKycStatus() {
		try {
			const res = await fetch(`${state.customerApiBase}/api/v1/users/kyc/status`, {
				headers: { ...customerAuthHeaders() }
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`KYC status failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			setStatus("#kycStatus", `KYC: ${data.kyc_status}`);
		} catch (e) {
			console.error(e);
			setStatus("#kycStatus", e.message || "Error", true);
		}
	}

	// Pump functions
	function loadPumpConfig() {
		const saved = localStorage.getItem("zappay.pump.config");
		if (saved) {
			try {
				const config = JSON.parse(saved);
				state.pumpApiBase = config.pumpApiBase;
				state.customerApiBase = config.customerApiBase;
				qs("#pumpApiBase").value = state.pumpApiBase;
				qs("#pumpCustomerApiBase").value = state.customerApiBase;
				const tokens = localStorage.getItem("zappay.pump.tokens");
				if (tokens) {
					const parsed = JSON.parse(tokens);
					state.pumpToken = parsed.access;
					setStatus("#pumpAuthStatus", "Session restored");
				}
			} catch (e) {
				console.error("Failed to load pump config:", e);
			}
		} else {
			state.pumpApiBase = "http://localhost:8001";
			state.customerApiBase = "http://localhost:8000";
			qs("#pumpApiBase").value = state.pumpApiBase;
			qs("#customerApiBase").value = state.customerApiBase;
		}
	}

	async function pumpLoadDashboard() {
		try {
			const pump_id = Number(qs('#dashPumpId').value || 1);
			const res = await fetch(`${state.customerApiBase}/api/v1/pumps/${pump_id}/dashboard`, {
				headers: { ...pumpAuthHeaders() }
			});
			if (!res.ok) throw new Error(await res.text());
			const data = await res.json();
			const s = qs('#p-dashboard-summary');
			s.innerHTML = `
				<div class="kv"><span>Total Transactions</span><span>${data.total_transactions || 0}</span></div>
				<div class="kv"><span>Total Revenue</span><span>₹ ${Number(data.total_revenue || 0).toFixed(2)}</span></div>
				<div class="kv"><span>Total Commission</span><span>₹ ${Number(data.total_commission || 0).toFixed(2)}</span></div>
				<div class="kv"><span>Today Txns</span><span>${data.transactions_today || 0}</span></div>
				<div class="kv"><span>Today Revenue</span><span>₹ ${Number(data.revenue_today || 0).toFixed(2)}</span></div>
			`;
		} catch (e) {
			console.error(e);
		}
	}

	let pTransPage = 1;
	async function pumpLoadTransactions(page) {
		try {
			pTransPage = page;
			const pump_id = Number(qs('#pumpHistId').value || 1);
			const res = await fetch(`${state.customerApiBase}/api/v1/transactions/pump/${pump_id}/history?page=${page}&page_size=10`, {
				headers: { ...pumpAuthHeaders() }
			});
			if (!res.ok) throw new Error(await res.text());
			const data = await res.json();
			const list = qs('#p-transactions-list');
			list.innerHTML = (data.transactions || []).map(tx =>
				`<div class="kv"><span>${tx.transaction_id} (${tx.transaction_type})</span><span>₹ ${Number(tx.amount || 0).toFixed(2)}</span></div>`
			).join('') || '<p class="muted">No transactions</p>';
			qs('#p-trans-page').textContent = `Page ${data.page} / ${data.total_pages}`;
		} catch (e) {
			console.error(e);
		}
	}

	async function addOperator() {
		try {
			setStatus('#opStatus', 'Adding operator…');
			const pump_id = Number(qs('#opPumpId').value || 1);
			const operator_phone = (qs('#opPhone').value || '').trim();
			const employee_id = (qs('#opEmployeeId').value || '').trim();
			if (!operator_phone) {
				setStatus('#opStatus', 'Operator phone required', true);
				return;
			}
			const url = `${state.customerApiBase}/api/v1/pumps/${pump_id}/operators?operator_phone=${encodeURIComponent(operator_phone)}&employee_id=${encodeURIComponent(employee_id)}`;
			const res = await fetch(url, { method: 'POST', headers: { ...pumpAuthHeaders() } });
			if (!res.ok) throw new Error(await res.text());
			setStatus('#opStatus', 'Operator added');
		} catch (e) {
			console.error(e);
			setStatus('#opStatus', e.message || 'Error', true);
		}
	}

	// WebSocket notifications for customer
	function parseJwtSub(token) {
		try {
			const payload = token.split('.')[1];
			const json = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
			return json.sub;
		} catch { return null; }
	}

	let ws;
	function connectCustomerSocket() {
		if (!state.customerToken || !state.customerApiBase) return;
		const sub = parseJwtSub(state.customerToken);
		if (!sub) return;
		const wsUrl = state.customerApiBase.replace(/^http/, 'ws') + `/ws/${sub}`;
		try {
			ws = new WebSocket(wsUrl);
			ws.onmessage = (evt) => {
				try {
					const msg = JSON.parse(evt.data);
					if (msg.event === 'wallet_recharged') {
						showToast(`Wallet +₹${msg.data.amount}. New: ₹${msg.data.new_balance}`);
						walletGetBalance();
					} else if (msg.event === 'fuel_purchase') {
						showToast(`Fuel purchase ₹${msg.data.amount}. Balance: ₹${msg.data.balance}`);
						customerLoadTransactions(1);
					}
				} catch {}
			};
			ws.onclose = () => {
				setTimeout(connectCustomerSocket, 3000);
			};
		} catch {}
	}

	function showToast(text) {
		const el = qs('#toast');
		if (!el) return;
		el.textContent = text;
		el.style.display = 'block';
		setTimeout(() => { el.style.display = 'none'; }, 3000);
	}

	function savePumpConfig() {
		const pumpBase = (qs("#pumpApiBase").value || "").trim();
		const customerBase = (qs("#pumpCustomerApiBase").value || "").trim();
		if (!pumpBase || !customerBase) {
			setStatus("#pumpConfigStatus", "Please provide both API URLs", true);
			return;
		}
		state.pumpApiBase = pumpBase.replace(/\/+$/, "");
		state.customerApiBase = customerBase.replace(/\/+$/, "");
		localStorage.setItem("zappay.pump.config", JSON.stringify({
			pumpApiBase: state.pumpApiBase,
			customerApiBase: state.customerApiBase
		}));
		setStatus("#pumpConfigStatus", `Saved: Pump ${state.pumpApiBase}, Customer ${state.customerApiBase}`);
	}

	async function pumpLogin() {
		try {
			setStatus("#pumpAuthStatus", "Logging in…");
			const phone = (qs("#pumpPhone").value || "").trim();
			const password = (qs("#pumpPassword").value || "").trim();
			if (!phone || !password) {
				setStatus("#pumpAuthStatus", "Phone and password required", true);
				return;
			}
			// For demo, we'll use the customer API for login and then use the token for pump operations
			const loginRes = await fetch(`${state.customerApiBase}/api/v1/auth/login`, {
				method: "POST",
				headers: { "Content-Type": "application/x-www-form-urlencoded" },
				body: new URLSearchParams({ username: phone, password })
			});
			if (!loginRes.ok) {
				const t = await loginRes.text();
				throw new Error(`Login failed: ${loginRes.status} ${t}`);
			}
			const data = await loginRes.json();
			state.pumpToken = data.access_token;
			localStorage.setItem("zappay.pump.tokens", JSON.stringify({ access: state.pumpToken, refresh: data.refresh_token }));
			setStatus("#pumpAuthStatus", "Logged in as pump operator");
		} catch (e) {
			console.error(e);
			setStatus("#pumpAuthStatus", e.message || "Login error", true);
		}
	}

	function pumpLogout() {
		state.pumpToken = null;
		localStorage.removeItem("zappay.pump.tokens");
		setStatus("#pumpAuthStatus", "Logged out");
	}

	function pumpAuthHeaders() {
		return state.pumpToken ? { Authorization: `Bearer ${state.pumpToken}` } : {};
	}

	async function pumpSignupSendOtp() {
		try {
			setStatus("#pumpSignupStatus", "Sending OTP…");
			const full_name = (qs("#pumpSignupName").value || "").trim();
			const phone_number = (qs("#pumpSignupPhone").value || "").trim();
			if (!full_name || !phone_number) {
				setStatus("#pumpSignupStatus", "Name and phone required", true);
				return;
			}
			if (!state.customerApiBase) {
				setStatus("#pumpSignupStatus", "Please configure Customer API URL first", true);
				return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/auth/register/otp/start`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ full_name, phone_number, role: "pump_owner" })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Send OTP failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			if (data.otp_debug) {
				setStatus("#pumpSignupStatus", `OTP sent. Debug: ${data.otp_debug}`);
			} else {
				setStatus("#pumpSignupStatus", "OTP sent. Check server logs.");
			}
		} catch (e) {
			console.error("Signup OTP error:", e);
			setStatus("#pumpSignupStatus", `Error: ${e.message || "Failed to fetch"}. Check Customer API URL in Configuration.`, true);
		}
	}

	async function pumpSignupComplete() {
		try {
			setStatus("#pumpSignupStatus", "Completing signup…");
			const full_name = (qs("#pumpSignupName").value || "").trim();
			const phone_number = (qs("#pumpSignupPhone").value || "").trim();
			const password = (qs("#pumpSignupPassword").value || "").trim();
			const otp_code = (qs("#pumpSignupOtp").value || "").trim();
			if (!full_name || !phone_number || !password || !otp_code) {
				setStatus("#pumpSignupStatus", "All fields required", true);
				return;
			}
			const res = await fetch(`${state.customerApiBase}/api/v1/auth/register/otp/complete`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ full_name, phone_number, password, otp_code, role: "pump_owner" })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Signup failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			state.pumpToken = data.access_token;
			localStorage.setItem("zappay.pump.tokens", JSON.stringify({ access: state.pumpToken, refresh: data.refresh_token }));
			setStatus("#pumpSignupStatus", "Signup complete & logged in");
		} catch (e) {
			console.error(e);
			setStatus("#pumpSignupStatus", e.message || "Error", true);
		}
	}

	async function validateQr(qrText) {
		try {
			setStatus("#scanStatus", "Validating QR…");
			const res = await fetch(`${state.pumpApiBase}/qr/validate`, {
				method: "POST",
				headers: { "Content-Type": "application/json", ...pumpAuthHeaders() },
				body: JSON.stringify({ qr_data: qrText })
			});
			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Validate failed: ${res.status} ${t}`);
			}
			const data = await res.json();
			if (!data.valid) {
				throw new Error("Invalid or expired QR code");
			}
			state.lastQrText = qrText;
			state.lastScanData = data;
			showPurchasePopup(data);
			setStatus("#scanStatus", "QR validated - opening purchase form");
		} catch (e) {
			console.error(e);
			setStatus("#scanStatus", e.message || "Validation error", true);
		}
	}

	function showPurchasePopup(data) {
		const el = qs("#customerDetails");
		el.innerHTML = `
			<div class="kv"><span>👤 Name</span><span>${escapeHtml(data.user_name || "")}</span></div>
			<div class="kv"><span>📱 Phone</span><span>${escapeHtml(data.user_phone || "")}</span></div>
			<div class="kv"><span>🚗 Vehicle</span><span>${escapeHtml(data.vehicle_number || "N/A")}</span></div>
			<div class="kv"><span>💰 Balance</span><span>₹ ${Number(data.wallet_balance || 0).toFixed(2)}</span></div>
		`;
		qs("#purchasePopup").style.display = "flex";
	}

	function hidePurchasePopup() {
		qs("#purchasePopup").style.display = "none";
		state.lastScanData = null;
		state.lastQrText = null;
	}

	async function purchaseFuel() {
		try {
			setStatus("#purchaseStatus", "Processing purchase…");
			if (!state.lastQrText || !state.lastScanData) {
				setStatus("#purchaseStatus", "No valid QR scanned", true);
				return;
			}

			const pump_id = Number(qs("#pumpId").value || 1);
			const fuel_type = qs("#fuelType").value || "petrol";
			const fuel_quantity = Number(qs("#fuelQuantity").value || 0);
			const fuel_rate = Number(qs("#fuelRate").value || 0);

			if (!fuel_quantity || !fuel_rate) {
				setStatus("#purchaseStatus", "Please enter quantity and rate", true);
				return;
			}

			const payload = {
				qr_code: state.lastQrText,
				pump_id,
				fuel_type,
				fuel_quantity,
				fuel_rate
			};

			const res = await fetch(`${state.pumpApiBase}/transactions/fuel-purchase`, {
				method: "POST",
				headers: { "Content-Type": "application/json", ...pumpAuthHeaders() },
				body: JSON.stringify(payload)
			});

			if (!res.ok) {
				const t = await res.text();
				throw new Error(`Purchase failed: ${res.status} ${t}`);
			}

			const result = await res.json();
			if (!result.success) {
				throw new Error(result.message || "Purchase failed");
			}

			setStatus("#purchaseStatus", `✅ Success! ₹${Number(result.data.amount || 0).toFixed(2)} charged. Balance: ₹${Number(result.data.remaining_balance || 0).toFixed(2)}`);
			setTimeout(() => {
				hidePurchasePopup();
				setStatus("#purchaseStatus", "");
			}, 3000);

		} catch (e) {
			console.error(e);
			setStatus("#purchaseStatus", e.message || "Purchase error", true);
		}
	}

	async function startScanner() {
		try {
			setStatus("#scanStatus", "Starting camera…");
			const readerEl = qs("#reader");
			if (state.html5Qr) {
				await stopScanner();
			}
			state.html5Qr = new Html5Qrcode("reader");
			const cameras = await Html5Qrcode.getCameras();
			const camId = cameras?.[0]?.id;
			if (!camId) throw new Error("No camera found");
			await state.html5Qr.start(
				camId,
				{
					fps: 10,
					qrbox: { width: 250, height: 250 }
				},
				decodedText => {
					if (decodedText && decodedText !== state.lastQrText) {
						validateQr(decodedText);
					}
				},
				() => {}
			);
			readerEl.classList.add("active");
			setStatus("#scanStatus", "Scanner ready - point camera at customer QR");
		} catch (e) {
			console.error(e);
			setStatus("#scanStatus", e.message || "Scanner error", true);
		}
	}

	async function stopScanner() {
		if (state.html5Qr) {
			try {
				await state.html5Qr.stop();
			} catch {}
			try {
				await state.html5Qr.clear();
			} catch {}
			state.html5Qr = null;
			qs("#reader").classList.remove("active");
		}
		setStatus("#scanStatus", "Scanner stopped");
	}

	function escapeHtml(s) {
		return String(s)
			.replaceAll("&", "&amp;")
			.replaceAll("<", "&lt;")
			.replaceAll(">", "&gt;")
			.replaceAll('"', "&quot;")
			.replaceAll("'", "&#039;");
	}

	function wireEvents() {
		// Role selection
		qs("#customerBtn").addEventListener("click", showCustomerInterface);
		qs("#pumpBtn").addEventListener("click", showPumpInterface);

		// Customer events
		qs("#customerSignupSendOtpBtn").addEventListener("click", customerSignupSendOtp);
		qs("#customerSignupCompleteBtn").addEventListener("click", customerSignupComplete);
		qs("#saveCustomerConfigBtn").addEventListener("click", saveCustomerConfig);
		qs("#customerLoginBtn").addEventListener("click", customerLogin);
		qs("#customerLogoutBtn").addEventListener("click", customerLogout);
		qs("#customerSendOtpLoginBtn").addEventListener("click", customerSendOtpLogin);
		qs("#customerOtpLoginBtn").addEventListener("click", customerOtpLogin);
		qs("#genQrBtn").addEventListener("click", generateQr);
		qs("#getProfileBtn").addEventListener("click", getProfile);
		qs("#walletTestRechargeBtn").addEventListener("click", walletTestRecharge);
		qs("#getWalletBalanceBtn").addEventListener("click", walletGetBalance);
		qs("#submitKycBtn").addEventListener("click", submitKyc);
		qs("#getKycStatusBtn").addEventListener("click", getKycStatus);
		qs("#c-trans-prev").addEventListener("click", () => customerLoadTransactions(Math.max(1, cTransPage - 1)));
		qs("#c-trans-next").addEventListener("click", () => customerLoadTransactions(cTransPage + 1));

		// Pump events
		qs("#pumpSignupSendOtpBtn").addEventListener("click", pumpSignupSendOtp);
		qs("#pumpSignupCompleteBtn").addEventListener("click", pumpSignupComplete);
		qs("#savePumpConfigBtn").addEventListener("click", savePumpConfig);
		qs("#pumpLoginBtn").addEventListener("click", pumpLogin);
		qs("#pumpLogoutBtn").addEventListener("click", pumpLogout);
		qs("#startScanBtn").addEventListener("click", startScanner);
		qs("#stopScanBtn").addEventListener("click", stopScanner);
		qs("#purchaseBtn").addEventListener("click", purchaseFuel);
		qs("#cancelPurchaseBtn").addEventListener("click", hidePurchasePopup);
		qs("#savePumpSettingsBtn").addEventListener("click", async () => {
			try {
				const pump_id = Number(qs("#setupPumpId").value || 1);
				const pump_name = (qs("#setupPumpName").value || "").trim();
				const petrol_price = Number(qs("#setupPetrolPrice").value || 0);
				const res = await fetch(`${state.pumpApiBase}/settings/save`, {
					method: "POST",
					headers: { "Content-Type": "application/json", ...pumpAuthHeaders() },
					body: JSON.stringify({ pump_id, pump_name, petrol_price })
				});
				if (!res.ok) {
					const t = await res.text();
					throw new Error(`Save failed: ${res.status} ${t}`);
				}
				setStatus("#pumpSetupStatus", "Settings saved");
			} catch (e) {
				console.error(e);
				setStatus("#pumpSetupStatus", e.message || "Error", true);
			}
		});
		qs("#loadPumpSettingsBtn").addEventListener("click", async () => {
			try {
				const pump_id = Number(qs("#setupPumpId").value || 1);
				const res = await fetch(`${state.pumpApiBase}/settings/${pump_id}`, {
					headers: { ...pumpAuthHeaders() }
				});
				if (!res.ok) {
					const t = await res.text();
					throw new Error(`Load failed: ${res.status} ${t}`);
				}
				const data = await res.json();
				if (data.pump_name) qs("#setupPumpName").value = data.pump_name;
				if (data.petrol_price) qs("#setupPetrolPrice").value = data.petrol_price;
				setStatus("#pumpSetupStatus", "Settings loaded");
			} catch (e) {
				console.error(e);
				setStatus("#pumpSetupStatus", e.message || "Error", true);
			}
		});
		qs("#loadPumpDashboardBtn").addEventListener("click", pumpLoadDashboard);
		qs("#loadPumpTransactionsBtn").addEventListener("click", () => pumpLoadTransactions(1));
		qs("#p-trans-prev").addEventListener("click", () => pumpLoadTransactions(Math.max(1, pTransPage - 1)));
		qs("#p-trans-next").addEventListener("click", () => pumpLoadTransactions(pTransPage + 1));
		qs("#addOperatorBtn").addEventListener("click", addOperator);
	}

	function init() {
		showRoleSelection();
		wireEvents();
		if (state.customerToken) connectCustomerSocket();
	}

	window.ZapPayDemo = { init };
})();


