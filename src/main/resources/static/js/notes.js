const API_NOTES = '/api/notes';
const token = localStorage.getItem('jwt');

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
    const res = await fetch(API_NOTES, {
        method:'POST',
        headers:{
            'Content-Type':'application/json',
            'Authorization':'Bearer '+token
        },
        body: JSON.stringify({
            title: document.getElementById('noteTitle').value,
            content: document.getElementById('noteContent').value
        })
    });
    if(res.ok) loadNotes();
    else alert('Failed to create note');
});

loadNotes();
