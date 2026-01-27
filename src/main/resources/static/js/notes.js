const API_NOTES = '/api/notes';
const token = localStorage.getItem('jwt');
const CSRF_COOKIE = 'XSRF-TOKEN';
const CSRF_HEADER = 'X-XSRF-TOKEN';

function getCsrfToken() {
    const raw = document.cookie
        .split(';')
        .map(cookie => cookie.trim())
        .find(cookie => cookie.startsWith(`${CSRF_COOKIE}=`));
    if (!raw) return null;
    return decodeURIComponent(raw.substring(CSRF_COOKIE.length + 1));
}

function addCsrfHeader(headers) {
    const token = getCsrfToken();
    if (token) headers[CSRF_HEADER] = token;
}

// Redirect if not logged in
if(!token) window.location.href = '/';

async function loadNotes(){
    const res = await fetch(API_NOTES, {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    if(res.ok){
        const notes = await res.json();
        const listDiv = document.getElementById('notesList');
        listDiv.innerHTML = '';
        notes.forEach(n => {
            const div = document.createElement('div');
            div.innerHTML = `<strong>${n.title}</strong><p>${n.content}</p>`;
            listDiv.appendChild(div);
        });
    } else {
        alert('Unauthorized');
        window.location.href = '/';
    }
}

document.getElementById('createNote').addEventListener('click', async () => {
    const headers = {
        'Content-Type':'application/json',
        'Authorization':'Bearer '+token
    };
    addCsrfHeader(headers);
    const res = await fetch(API_NOTES, {
        method:'POST',
        headers,
        body: JSON.stringify({
            title: document.getElementById('noteTitle').value,
            content: document.getElementById('noteContent').value
        })
    });
    if(res.ok) loadNotes();
    else alert('Failed to create note');
});

loadNotes();
