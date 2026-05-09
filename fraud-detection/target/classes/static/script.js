const app = document.getElementById("app");

// Theme management
function initTheme() {
  const savedTheme = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', savedTheme);
  updateThemeIcon(savedTheme);
}

function toggleTheme() {
  const currentTheme = document.documentElement.getAttribute('data-theme');
  const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem('theme', newTheme);
  updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
  const icon = document.getElementById('theme-icon');
  if (icon) {
    icon.textContent = theme === 'dark' ? '🌙' : '☀️';
  }
}

initTheme();

function showLogin() {
  app.innerHTML = `
    <h2>Welcome Back</h2>
    <input id="name" placeholder="Username"><br>
    <input type="password" id="password" placeholder="Password"><br>
    <button onclick="login()">Login</button><br>
    <button class="secondary" onclick="showRegister()">Create Account</button>
    <button class="secondary" style="border:none;" onclick="showForgotPassword()">Forgot Password?</button>
  `;
}

function showForgotPassword() {
  app.innerHTML = `
    <h2>Reset Password</h2>
    <input id="resetName" placeholder="Username"><br>
    <input type="password" id="newPassword" placeholder="New Password"><br>
    <button onclick="resetPassword()">Reset Password</button><br>
    <button class="secondary" onclick="showLogin()">Back to Login</button>
  `;
}

async function resetPassword() {
  const name = document.getElementById("resetName").value;
  const newPassword = document.getElementById("newPassword").value;

  if (!name || !newPassword) {
    alert("Please fill all fields.");
    return;
  }

  const res = await fetch("/api/reset-password", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, password: newPassword })
  });

  if (res.ok && await res.text()) {
    alert("Password reset successfully!");
    showLogin();
  } else {
    alert("Username not found.");
  }
}

function showRegister() {
  app.innerHTML = `
    <h2>Create Account</h2>
    <input id="id" placeholder="User ID"><br>
    <input id="name" placeholder="Username"><br>
    <input type="password" id="password" placeholder="Password"><br>
    <input type="password" id="confirmPassword" placeholder="Confirm Password"><br>
    <button onclick="register()">Register</button><br>
    <button class="secondary" onclick="showLogin()">Back to Login</button>
  `;
}

async function register() {
  const id = document.getElementById("id").value;
  const name = document.getElementById("name").value;
  const pwd = document.getElementById("password").value;
  const cpwd = document.getElementById("confirmPassword").value;

  if (!id || !name || !pwd || !cpwd) {
    alert("Please fill all fields.");
    return;
  }

  if (pwd !== cpwd) {
    alert("Passwords do not match!");
    return;
  }

  await fetch("/api/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ id, name, password: pwd })
  });

  alert("Registered successfully!");
  showLogin();
}

async function login() {
  const name = document.getElementById("name").value;
  const password = document.getElementById("password").value;

  if (!name || !password) {
    alert("Please fill all fields. If you do not have an account, please click Create Account to register.");
    return;
  }

  const res = await fetch("/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, password })
  });

  const text = await res.text();
  if (!text) {
    alert("Invalid username or password. If you do not have an account, please click Create Account to register.");
    return;
  }

  try {
    const user = JSON.parse(text);
    if (user && user.id) {
      alert("Login successful!");
      showDashboard(user.id);
    } else {
      alert("Invalid username or password. If you do not have an account, please click Create Account to register.");
    }
  } catch (e) {
    alert("An error occurred during login. Please try again.");
  }
}

function showDashboard(userId) {
  app.innerHTML = `
      <h2>Transaction Dashboard</h2>
      <h3>Check Individual Transaction</h3>
      <div class="input-grid">
          <input type="number" id="amount" placeholder="Amount (e.g. 500)" min="0" onkeydown="if(event.key==='-' || event.key==='e' || event.key==='E') event.preventDefault();">
          
          <select id="location">
              <option value="" disabled selected>Select Country</option>
              <option value="USA">USA</option>
              <option value="India">India</option>
              <option value="UK">UK</option>
              <option value="Canada">Canada</option>
              <option value="Australia">Australia</option>
              <option value="Germany">Germany</option>
              <option value="France">France</option>
              <option value="Japan">Japan</option>
              <option value="Nigeria">Nigeria</option>
              <option value="Russia">Russia</option>
          </select>
          
          <input id="merchant" placeholder="Merchant (e.g. Amazon)">
          
          <select id="deviceType">
              <option value="" disabled selected>Select Device</option>
              <option value="Mobile">Mobile</option>
              <option value="Desktop">Desktop</option>
              <option value="Tablet">Tablet</option>
              <option value="Emulator">Emulator</option>
          </select>

          <input id="ipAddress" placeholder="IP Address (e.g. 192.168.1.1)">
          <input type="date" id="date">
          <input type="time" id="time">
          
          <select id="paymentType">
              <option value="" disabled selected>Select Payment Type</option>
              <option value="Credit Card">Credit Card</option>
              <option value="Debit Card">Debit Card</option>
              <option value="Crypto">Crypto</option>
              <option value="Bank Transfer">Bank Transfer</option>
              <option value="Netbanking">Netbanking</option>
              <option value="UPI">UPI</option>
          </select>
      </div>
      <br>
      <button onclick="send('${userId}')">Analyze Transaction</button>
      <div id="res-container"></div>
      
      <div class="file-upload-wrapper">
          <h3>Batch CSV Analysis</h3>
          <p style="margin-bottom: 1rem; color: #cbd5e1; font-size: 0.9rem;">Format: userId, amount, location, merchant, deviceType, ipAddress, date, time, paymentType</p>
          <input type="file" id="csvFile" accept=".csv" style="padding: 0.5rem; background: transparent; border: 1px dashed rgba(255,255,255,0.3);">
          <button onclick="uploadCsv()">Upload and Analyze</button>
          <button class="secondary" style="background: #ef4444; border:none; margin-top:0.5rem;" onclick="clearHistory()">Clear All Transactions</button>
          <div id="csv-results" style="overflow-x: auto;"></div>
      </div>
    `;
}

async function clearHistory() {
  if (!confirm("Are you sure you want to clear all transaction history? This cannot be undone.")) return;
  
  try {
    const res = await fetch("/api/transactions", { method: "DELETE" });
    if (res.ok) {
      alert("All transactions cleared successfully.");
      document.getElementById("csv-results").innerHTML = "";
    } else {
      alert("Failed to clear transactions.");
    }
  } catch (e) {
    alert("Error clearing history.");
  }
}

function getStatusClass(status) {
  if (status.includes("High Severity")) return "fraud";
  if (status.includes("Medium Severity")) return "review";
  return "safe";
}

function isValidIP(ip) {
  const regex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
  return regex.test(ip);
}

async function send(id) {
  const amountInput = document.getElementById("amount");
  if (amountInput.validity && amountInput.validity.badInput) {
    alert("Please enter a valid numeric amount (no special characters or negative signs).");
    return;
  }

  const amount = amountInput.value;
  const location = document.getElementById("location").value;
  const merchant = document.getElementById("merchant").value;
  const deviceType = document.getElementById("deviceType").value;
  const ipAddress = document.getElementById("ipAddress").value;
  const date = document.getElementById("date").value;
  const time = document.getElementById("time").value;
  const paymentType = document.getElementById("paymentType").value;

  if (!amount || !location || !merchant || !deviceType || !ipAddress || !date || !paymentType) {
    alert("Please fill out all fields.");
    return;
  }

  if (!isValidIP(ipAddress)) {
    alert("Please enter a valid IP address.");
    return;
  }

  const res = await fetch("/api/transaction", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      transactionId: Date.now().toString(),
      userId: id,
      amount: parseFloat(amount),
      location,
      merchant,
      deviceType,
      ipAddress,
      date,
      time,
      paymentType,
      timestamp: Date.now()
    })
  });

  const data = await res.json();
  const statusClass = getStatusClass(data.status);

  document.getElementById("res-container").innerHTML = `
    <div class="result-box ${statusClass}">
        <div style="font-size:0.8rem; color:#cbd5e1; margin-bottom:0.5rem;">Txn ID: ${data.transactionId}</div>
        <div style="font-size:1.2rem;">${data.status}</div>
    </div>
  `;
}

async function uploadCsv() {
  const fileInput = document.getElementById("csvFile");
  if (!fileInput.files.length) {
    alert("Please select a file first");
    return;
  }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);

  document.getElementById("csv-results").innerHTML = "<p>Analyzing...</p>";

  try {
    const res = await fetch("/api/upload", {
      method: "POST",
      body: formData
    });

    if (!res.ok) throw new Error("Server error");

    const data = await res.json();

    if (!data || data.length === 0) {
      document.getElementById("csv-results").innerHTML = "<p style='color:#f59e0b'>No transactions were processed. Please ensure your CSV has 9 columns matching the specified format and includes a header row.</p>";
      return;
    }

    // Calculate Summary
    const summary = {
      high: 0,
      medium: 0,
      low: 0,
      safe: 0,
      matches: 0,
      totalWithExpected: 0
    };

    data.forEach(txn => {
      // Risk Counts
      if (txn.status.includes("High Severity")) summary.high++;
      else if (txn.status.includes("Medium Severity")) summary.medium++;
      else if (txn.status.includes("Low Risk")) summary.low++;
      else summary.safe++;

      // Accuracy Calculation
        if (txn.expectedStatus) {
          summary.totalWithExpected++;
          const isDetectedFraud = !txn.status.toLowerCase().includes("safe");
          const isExpectedFraud = txn.expectedStatus.toLowerCase().includes("fraud") || txn.expectedStatus.toLowerCase().includes("risky");
          
          if (isDetectedFraud === isExpectedFraud) {
            summary.matches++;
          }
        }
    });

    const accuracy = summary.totalWithExpected > 0 
      ? ((summary.matches / summary.totalWithExpected) * 100).toFixed(2) 
      : null;

    // Define "Fraud" as anything not "Safe"
    const fraudCount = summary.high + summary.medium + summary.low;

    let summaryHTML = `
      <div style="margin-bottom: 1.5rem; padding: 1rem; background: var(--btn-secondary-bg); border-radius: 10px; border-left: 4px solid #38bdf8; display: flex; flex-wrap: wrap; gap: 1.5rem; font-size: 0.95rem; align-items: center;">
        <div><strong>Total Transactions:</strong> <span style="color: #38bdf8; font-weight: 700;">${data.length}</span></div>
        <div><strong>Safe:</strong> <span style="color: #10b981; font-weight: 700;">${summary.safe}</span></div>
        <div><strong>Fraud:</strong> <span style="color: #ef4444; font-weight: 700;">${fraudCount}</span></div>
        ${accuracy !== null ? `
          <div><strong>Accuracy:</strong> <span style="color: #818cf8; font-weight: 700;">${accuracy}%</span></div>
        ` : ''}
      </div>
    `;

    let tableHTML = summaryHTML + `
            <table>
                <thead>
                    <tr>
                        <th>Transaction ID</th>
                        <th>User ID</th>
                        <th>Amount</th>
                        <th>Merchant</th>
                        <th>Location</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
        `;

    data.forEach(txn => {
      const statusClass = getStatusClass(txn.status);
      
      tableHTML += `
                <tr>
                    <td style="font-family: monospace;">${txn.transactionId}</td>
                    <td>${txn.userId}</td>
                    <td>$${txn.amount}</td>
                    <td>${txn.merchant}</td>
                    <td>${txn.location}</td>
                    <td class="result-box ${statusClass}" style="margin-top:0; padding:0.25rem 0.5rem; background:transparent; border-left:none;">${txn.status}</td>
                </tr>
            `;
    });

    tableHTML += "</tbody></table>";
    document.getElementById("csv-results").innerHTML = tableHTML;
  } catch (e) {
    console.error(e);
    document.getElementById("csv-results").innerHTML = "<p style='color:#ef4444'>Invalid CSV file format. Please ensure your CSV uses commas as separators, has exactly 9 columns, and the data matches the expected types.</p>";
  }
}

showLogin();