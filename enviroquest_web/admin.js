/**
 * ================================================================
 * 🌿 ENVIROQUEST ADMIN CORE ENGINE
 * Developer: Gemini AI x Admin
 * ================================================================
 */

// --- 1. FIREBASE CONNECTION & SECURITY ---
const firebaseConfig = {
    apiKey: "AIzaSyDbJ8rFzwJWldBueoA76OP7ajSXDGyMdH8",
    authDomain: "enviroquest-4bb68.firebaseapp.com",
    projectId: "enviroquest-4bb68",
    storageBucket: "enviroquest-4bb68.firebasestorage.app",
    messagingSenderId: "633387091098",
    appId: "1:633387091098:web:b4824a35c6acf13e6c4b23"
};

firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();
const auth = firebase.auth();

// Authentication Guard: Redirect to login if not authenticated
auth.onAuthStateChanged(user => { 
    if (!user) window.location.href = "login.html"; 
});

/**
 * --- 2. APP LIFECYCLE ---
 */
let wasteChart, rateChart;

document.addEventListener("DOMContentLoaded", () => {
    initCharts();      // Initialize visual analytics
    setupListeners();  // Establish real-time Firestore listeners
});

/**
 * --- 3. NAVIGATION & UI LOGIC ---
 */

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('collapsed');
    document.getElementById('mainContent').classList.toggle('expanded');
}

function showSection(id, btn) {
    // Hide all sections, display the selected one
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');
    
    // Highlight active link in sidebar
    document.querySelectorAll('.sidebar .nav-link').forEach(l => l.classList.remove('active'));
    if(btn) btn.classList.add('active');

    // Auto-reset User Tab to "Pending" when navigating to User Management
    if (id === 'users-section') {
        const firstTab = document.querySelector('#userTabs .nav-link:first-child');
        if (firstTab) switchUserTab('pending', firstTab);
    }
}

function logoutAdmin() { 
    if (confirm("Are you sure you want to log out?")) {
        auth.signOut().then(() => window.location.href = "login.html"); 
    }
}

/**
 * --- 4. REAL-TIME DATA HUB ---
 */
function setupListeners() {
    
    // 4.1 MONITORING: Pending Users & Notifications
    db.collection("Users").where("status", "==", "pending").onSnapshot(snap => {
        const table = document.getElementById('pendingTableBody');
        const badge = document.getElementById('notif-badge');
        const list = document.getElementById('notification-list');

        // Update statistical labels
        ['pendingCount', 'stat-pending'].forEach(id => {
            const el = document.getElementById(id);
            if(el) el.innerText = snap.size;
        });

        // Toggle Notification Badge visibility
        if (badge) {
            badge.innerText = snap.size;
            badge.style.display = snap.size > 0 ? "block" : "none";
        }

        let tableHtml = ""; let notifHtml = "";
        snap.forEach(doc => {
            const u = doc.data();
            tableHtml += `
                <tr>
                    <td class="ps-4"><b>${u.fullname}</b><br><small>${u.email}</small></td>
                    <td>${u.address}</td>
                    <td>${u.dateRegistered || 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-success me-1 shadow-sm" onclick="approveUser('${doc.id}', '${u.fullname}')">Approve</button>
                        <button class="btn btn-sm btn-outline-danger" onclick="rejectUser('${doc.id}', '${u.fullname}')">Reject</button>
                    </td>
                </tr>`;

            notifHtml += `
                <a href="#" class="dropdown-item d-flex align-items-center p-3 border-bottom" onclick="showSection('users-section')">
                    <div class="bg-warning text-white rounded-circle p-2 me-3 shadow-sm"><i class="bi bi-person-plus"></i></div>
                    <div>
                        <div class="small fw-bold">New Resident Registration</div>
                        <div class="small text-muted">${u.fullname} is awaiting approval.</div>
                    </div>
                </a>`;
        });
        table.innerHTML = tableHtml || '<tr><td colspan="4" class="text-center p-4">No pending registration requests.</td></tr>';
        list.innerHTML = notifHtml || '<div class="p-3 text-center text-muted small">No new notifications</div>';
        
        updateRateChart(); 
    });

    // 4.2 LISTENER: Approved & Verified Residents
    db.collection("Users").where("status", "==", "approved").onSnapshot(snap => {
        const statTotal = document.getElementById('stat-total');
        if(statTotal) statTotal.innerText = snap.size;
        
        const tableBody = document.getElementById('approvedTableBody');
        let html = "";
        snap.forEach(doc => {
            const u = doc.data();
            html += `
                <tr>
                    <td class="ps-4"><b>${u.fullname}</b><br><small>${u.email}</small></td>
                    <td>${u.address}</td>
                    <td><span class="badge rounded-pill bg-success-soft text-success px-3 border border-success">Verified</span></td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-danger" onclick="rejectUser('${doc.id}', '${u.fullname}')" title="Revoke Access">
                            <i class="bi bi-person-x"></i>
                        </button>
                    </td>
                </tr>`;
        });
        if(tableBody) tableBody.innerHTML = html || '<tr><td colspan="4" class="text-center p-4">No verified users found.</td></tr>';
        updateRateChart();
    });

    // 4.3 LISTENER: Rejected Residents (Archive)
    db.collection("Users").where("status", "==", "rejected").onSnapshot(snap => {
        const statRejected = document.getElementById('stat-rejected');
        if(statRejected) statRejected.innerText = snap.size;
        
        const tableBody = document.getElementById('rejectedTableBody');
        let html = "";
        snap.forEach(doc => {
            const u = doc.data();
            html += `
                <tr>
                    <td class="ps-4"><b>${u.fullname}</b></td>
                    <td>${u.address}</td>
                    <td class="text-danger small italic">${u.rejectReason || 'No reason specified'}</td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-success" onclick="restoreUser('${doc.id}')">
                            <i class="bi bi-arrow-clockwise"></i> Restore
                        </button>
                    </td>
                </tr>`;
        });
        if(tableBody) tableBody.innerHTML = html || '<tr><td colspan="4" class="text-center p-4 text-muted small">Archive is empty.</td></tr>';
        updateRateChart();
    });

    // 4.4 LISTENER: Waste Submissions (Quality Control Desk)
    db.collection("Submissions").where("status", "==", "Pending").onSnapshot(async snap => {
        const tableBody = document.getElementById("verificationTableBody");
        const pendingStat = document.getElementById("stat-pending-verification"); 
        
        if (pendingStat) pendingStat.innerText = snap.size;

        if (snap.empty) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center py-5">
                        <div class="py-4">
                            <i class="bi bi-clipboard-check text-muted" style="font-size: 3rem; opacity: 0.3;"></i>
                            <p class="text-muted mt-3 fw-medium">Queue Clear. No pending verifications.</p>
                        </div>
                    </td>
                </tr>`;
            return;
        }

        const promises = snap.docs.map(async (doc) => {
            const sub = doc.data();
            const userDoc = await db.collection("Users").doc(sub.userId).get();
            const userData = userDoc.exists ? userDoc.data() : { fullname: "Unknown Resident", address: "N/A" };

            return `
                <tr class="border-bottom border-light-subtle">
                    <td class="ps-4 py-4">
                        <div class="d-flex align-items-center">
                            <div class="resident-avatar me-3">
                                <span class="badge bg-light text-primary rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm" style="width: 42px; height: 42px; font-size: 1rem; border: 1px solid #e2e8f0;">
                                    ${userData.fullname.charAt(0)}
                                </span>
                            </div>
                            <div>
                                <h6 class="m-0 fw-bold text-dark">${userData.fullname}</h6>
                                <p class="m-0 text-muted small"><i class="bi bi-geo-alt"></i> ${userData.address}</p>
                            </div>
                        </div>
                    </td>
                    <td class="py-4">
                        <div class="position-relative d-inline-block">
                            <a href="${sub.proofImageUrl}" target="_blank">
                                <img src="${sub.proofImageUrl}" class="rounded-3 shadow-sm border verification-img" 
                                    style="width: 55px; height: 55px; object-fit: cover; transition: all 0.3s; border: 2px solid white !important;">
                            </a>
                        </div>
                    </td>
                    <td class="py-4">
                        <span class="small fw-bold text-secondary px-3 py-1 bg-light rounded-pill border d-inline-flex align-items-center">
                            <span class="status-dot me-2"></span>${sub.questTitle}
                        </span>
                    </td>
                    <td class="pe-4 py-4 text-end">
                        <div class="d-inline-flex gap-2">
                            <button class="btn-action btn-approve-soft" title="Approve Submission" onclick="handleVerification('${doc.id}', '${sub.userId}', '${sub.questId}', 'Approved')">
                                <i class="bi bi-check2"></i>
                            </button>
                            <button class="btn-action btn-reject-soft" title="Decline Submission" onclick="handleVerification('${doc.id}', '${sub.userId}', '${sub.questId}', 'Rejected')">
                                <i class="bi bi-trash3"></i>
                            </button>
                        </div>
                    </td>
                </tr>`;
        });

        const allRows = await Promise.all(promises);
        tableBody.innerHTML = allRows.join('');
    });
}

/**
 * --- 5. ACTION HANDLERS ---
 */

function approveUser(id, name) {
    if (confirm(`Confirm approval for ${name}?`)) {
        db.collection("Users").doc(id).update({ 
            status: "approved", 
            dateApproved: new Date().toLocaleString() 
        });
    }
}

function rejectUser(id, name) {
    const reason = prompt(`Please specify the reason for rejecting ${name}:`);
    if (reason) {
        db.collection("Users").doc(id).update({ 
            status: "rejected", 
            rejectReason: reason, 
            dateRejected: new Date().toLocaleString() 
        });
    }
}

async function handleVerification(submissionId, userId, questId, newStatus) {
    if (!confirm(`Are you sure you want to set this submission to ${newStatus}?`)) return;
    try {
        const batch = db.batch();
        batch.update(db.collection("Submissions").doc(submissionId), { 
            status: newStatus, 
            verifiedAt: new Date().toLocaleString() 
        });

        if (newStatus === 'Approved') {
            const questDoc = await db.collection("Quests").doc(questId).get();
            if (questDoc.exists) {
                const pts = questDoc.data().points || 0;
                batch.update(db.collection("Users").doc(userId), { 
                    totalPoints: firebase.firestore.FieldValue.increment(pts) 
                });
            }
        }
        await batch.commit();
        alert(`Process Complete: Submission has been ${newStatus}.`);
    } catch (e) { 
        console.error("Verification Error:", e);
        alert("An error occurred during verification. Please check the logs.");
    }
}

/**
 * --- 6. QUEST MANAGEMENT ---
 */

db.collection("Quests").where("status", "==", "Active").onSnapshot(snap => {
    const table = document.getElementById('questTableBody');
    let html = "";
    snap.forEach(doc => {
        const q = doc.data();
        html += `
            <tr>
                <td class="ps-4"><b>${q.title}</b><br><small class="text-muted">${q.description.substring(0,40)}...</small></td>
                <td><span class="badge bg-primary px-3">${q.points} pts</span></td>
                <td><span class="badge bg-success-soft text-success">Active</span></td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger shadow-sm" onclick="moveToTrash('${doc.id}')">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>`;
    });
    if(table) table.innerHTML = html || '<tr><td colspan="4" class="text-center p-4">No active quests found. Create one to get started.</td></tr>';
});

db.collection("Quests").where("status", "==", "Trash").onSnapshot(snap => {
    const table = document.getElementById('trashQuestTableBody');
    let html = "";
    snap.forEach(doc => {
        const q = doc.data();
        html += `
            <tr>
                <td class="ps-4"><s>${q.title}</s></td>
                <td>${q.points} pts</td>
                <td><span class="badge bg-secondary">Archived</span></td>
                <td class="text-center">
                    <button class="btn btn-sm btn-success" onclick="restoreQuest('${doc.id}')">
                        <i class="bi bi-arrow-clockwise"></i> Restore
                    </button>
                </td>
            </tr>`;
    });
    if(table) table.innerHTML = html || '<tr><td colspan="4" class="text-center p-4 text-muted small">Trash is empty.</td></tr>';
});

function createQuest(event) {
    event.preventDefault();
    const title = document.getElementById('questTitle').value;
    const points = parseInt(document.getElementById('questPoints').value);
    const desc = document.getElementById('questDesc').value;

    db.collection("Quests").add({
        title: title, 
        points: points, 
        description: desc, 
        status: "Active", 
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
    }).then(() => {
        bootstrap.Modal.getInstance(document.getElementById('questModal')).hide();
        document.getElementById('questForm').reset();
    });
}

function moveToTrash(id) {
    if(confirm("Move this quest to archive?")) {
        db.collection("Quests").doc(id).update({ 
            status: "Trash", 
            deletedAt: new Date().toLocaleString() 
        });
    }
}

/**
 * --- 7. ANALYTICS ENGINE (Chart.js) ---
 */
function initCharts() {
    const ctxWaste = document.getElementById('wasteChart').getContext('2d');
    wasteChart = new Chart(ctxWaste, {
        type: 'bar',
        data: {
            labels: ['Plastic', 'Paper', 'Glass', 'Metal', 'Organic'],
            datasets: [{
                label: 'Kilograms Collected',
                data: [12, 19, 3, 5, 2], 
                backgroundColor: 'rgba(34, 197, 94, 0.7)',
                borderRadius: 8
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });

    const ctxRate = document.getElementById('rateChart').getContext('2d');
    rateChart = new Chart(ctxRate, {
        type: 'doughnut',
        data: {
            labels: ['Approved', 'Rejected', 'Pending'],
            datasets: [{
                data: [0, 0, 0],
                backgroundColor: ['#22c55e', '#ef4444', '#f59e0b'],
                hoverOffset: 10,
                borderWidth: 0
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, cutout: '75%' }
    });
}

function updateRateChart() {
    if(!rateChart) return;
    const a = parseInt(document.getElementById('stat-total').innerText) || 0;
    const r = parseInt(document.getElementById('stat-rejected').innerText) || 0;
    const p = parseInt(document.getElementById('stat-pending').innerText) || 0;
    rateChart.data.datasets[0].data = [a, r, p];
    rateChart.update();
}

/**
 * --- 8. SUB-MENU NAVIGATION ---
 */

function switchUserTab(tab, btn) {
    ['pending', 'approved', 'rejected'].forEach(t => {
        document.getElementById('view-' + t).style.display = (tab === t) ? 'block' : 'none';
    });
    document.querySelectorAll('#userTabs .nav-link').forEach(l => l.classList.remove('active'));
    btn.classList.add('active');
}

function switchQuestTab(tab, btn) {
    document.getElementById('view-active').style.display = (tab === 'active') ? 'block' : 'none';
    document.getElementById('view-trash').style.display = (tab === 'trash') ? 'block' : 'none';
    const navLinks = btn.closest('.nav').querySelectorAll('.nav-link');
    navLinks.forEach(link => link.classList.remove('active'));
    btn.classList.add('active');
}