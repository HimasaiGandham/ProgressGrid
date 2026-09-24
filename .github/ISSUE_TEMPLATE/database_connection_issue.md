---
name: "Database / Connection Issue"
about: Report problems connecting Frontend, Backend, or MySQL Database
title: "[DB/CONN]: "
labels: ["bug", "database", "connection"]
assignees: ""
---

### Problem Description
<!-- Detail the connection or persistence failure. -->

### Symptoms
- [ ] Activities/habits missing after page refresh
- [ ] Backend fails to start (CommunicationsException / Access Denied)
- [ ] Frontend falls back to demo mode silently
- [ ] CORS error in browser console
- [ ] 502 Bad Gateway from dev_server.py

### Environment Details
- **MySQL Running**: [Yes/No]
- **Port**: [e.g. 3306, 8080, 3000, 5500]
- **Frontend Origin**: [e.g. http://localhost:3000, GitHub Pages]

### Steps to Reproduce
1. Start backend / frontend
2. Attempt action: '...'
3. Check browser network tab and terminal logs

### Logs / Error Messages
<!-- Spring Boot stack trace, dev_server logs, or MySQL error logs -->
