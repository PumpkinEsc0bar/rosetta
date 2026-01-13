const API_BASE = '/auth';

// Tabs
const loginTab = document.getElementById('tab-login');
const registerTab = document.getElementById('tab-register');
const loginFormDiv = document.getElementById('loginForm');
const registerFormDiv = document.getElementById('registerForm');
const tabsContainer = document.getElementById('tabsContainer');

function switchTab(tab){
    if(tab === 'login'){
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
        loginFormDiv.classList.add('active');
        registerFormDiv.classList.remove('active');
    } else {
        registerTab.classList.add('active');
        loginTab.classList.remove('active');
        registerFormDiv.classList.add('active');
        loginFormDiv.classList.remove('active');
    }
}

loginTab.addEventListener('click', () => switchTab('login'));
registerTab.addEventListener('click', () => switchTab('register'));

// Register
document.getElementById('registerBtn').addEventListener('click', async () => {
    const res = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({
            username: document.getElementById('regUsername').value,
            email: document.getElementById('regEmail').value,
            password: document.getElementById('regPassword').value
        })
    });
    if(res.ok){
        alert('Registered! Please login.');
        switchTab('login');
    } else alert('Registration failed');
});

// Login
document.getElementById('loginBtn').addEventListener('click', async () => {
    const res = await fetch(`${API_BASE}/login`, {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({
            username: document.getElementById('loginUsername').value,
            password: document.getElementById('loginPassword').value
        })
    });
    if(res.ok){
        const data = await res.json();
        localStorage.setItem('jwt', data.token);
        localStorage.setItem('username', document.getElementById('loginUsername').value);
        showLoggedIn();
    } else alert('Login failed');
});

// Logout
function logout(){
    localStorage.removeItem('jwt');
    localStorage.removeItem('username');
    showLoggedIn();
}

document.addEventListener('click', e=>{
    if(e.target && e.target.id==='headerLogout') logout();
});

function showLoggedIn(){
    const username = localStorage.getItem('username');
    const header = document.getElementById('headerContainer');
    if(username){

        tabsContainer.style.display='none';
        loginFormDiv.style.display='none';
        registerFormDiv.style.display='none';

        header.style.display='block';
        const span = document.getElementById('headerUsername');
        if(span) span.innerText = username;
    } else {
        tabsContainer.style.display='block';
        switchTab('login');

        header.style.display='none';
    }
}

document.addEventListener('DOMContentLoaded', showLoggedIn);
