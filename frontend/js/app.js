// app.js - Main Application Logic (Routing & UI State)

document.addEventListener('DOMContentLoaded', () => {
    
    // Navigation
    const navLinks = document.querySelectorAll('.nav-link');
    const sections = document.querySelectorAll('.content-section');

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Remove active from all links and sections
            navLinks.forEach(l => l.classList.remove('active'));
            sections.forEach(s => s.classList.add('hidden'));
            sections.forEach(s => s.classList.remove('active'));
            
            // Add active to clicked link
            link.classList.add('active');
            
            // Show target section
            const targetId = link.getAttribute('data-target');
            const targetSection = document.getElementById(targetId);
            targetSection.classList.remove('hidden');
            targetSection.classList.add('active');
        });
    });

    // Date in welcome banner
    const dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    document.getElementById('today-date').textContent = new Date().toLocaleDateString('en-US', dateOptions);

    // Initial Load when auth is ready
    window.addEventListener('app-ready', () => {
        if(window.DashboardManager) {
            window.DashboardManager.init();
        }
    });
});
