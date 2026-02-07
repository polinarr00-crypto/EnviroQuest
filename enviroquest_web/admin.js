const sampleQuests = [
    { id: 1, name: "Ryan Polinar", photo: "https://via.placeholder.com/400/2ecc71/FFFFFF?text=Plastic+Waste", type: "Plastic", date: "2025-12-01" },
    { id: 2, name: "Cristiana Orias", photo: "https://via.placeholder.com/400/3498db/FFFFFF?text=Paper+Waste", type: "Paper", date: "2025-12-01" },
    { id: 3, name: "Christian Macato", photo: "https://via.placeholder.com/400/e67e22/FFFFFF?text=Metal+Waste", type: "Metal", date: "2025-12-01" }
];

function renderTable() {
    const tableBody = document.getElementById('quest-table-body');
    tableBody.innerHTML = "";
    
    sampleQuests.forEach(quest => {
        tableBody.innerHTML += `
            <tr id="row-${quest.id}">
                <td class="align-middle"><strong>${quest.name}</strong></td>
                <td class="align-middle">
                    <img src="${quest.photo}" class="img-preview" onclick="openModal('${quest.photo}')" data-bs-toggle="modal" data-bs-target="#imageModal">
                </td>
                <td class="align-middle"><span class="badge bg-info text-dark">${quest.type}</span></td>
                <td class="align-middle">${quest.date}</td>
                <td class="align-middle">
                    <button class="btn btn-sm btn-success" onclick="approve(${quest.id})">Approve</button>
                    <button class="btn btn-sm btn-danger" onclick="reject(${quest.id})">Reject</button>
                </td>
            </tr>
        `;
    });
}

function approve(id) {
    if(confirm("Approve this waste submission?")) {
        document.getElementById(`row-${id}`).remove();
        updateCount();
    }
}

function reject(id) {
    let reason = prompt("Reason for rejection:");
    if(reason) {
        document.getElementById(`row-${id}`).remove();
        updateCount();
    }
}

function updateCount() {
    const count = document.getElementById('quest-table-body').children.length;
    document.getElementById('pending-count').innerText = count;
}

function openModal(src) {
    document.getElementById('modalImg').src = src;
}

renderTable();