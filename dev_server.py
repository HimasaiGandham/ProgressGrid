import http.server
import socketserver
import os
import sys
import urllib.request
import urllib.error

PORT = 3000
BACKEND_URL = "http://localhost:8080"
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
FRONT_END_DIR = os.path.join(BASE_DIR, "Front-End")

class LiveDevelopmentHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=FRONT_END_DIR, **kwargs)

    def end_headers(self):
        # Force browser to never cache during live development & testing
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE")
        self.send_header("Access-Control-Allow-Headers", "*")
        super().end_headers()

    def do_OPTIONS(self):
        if self.path.startswith("/api"):
            self.proxy_request()
        else:
            self.send_response(200)
            self.end_headers()

    def do_GET(self):
        if self.path.startswith("/api"):
            self.proxy_request()
        else:
            # Default root request to login.html
            clean_path = self.path.split('?')[0]
            if clean_path in ("/", ""):
                query = ("?" + self.path.split('?')[1]) if '?' in self.path else ""
                self.path = "/login.html" + query
            super().do_GET()

    def do_POST(self):
        if self.path.startswith("/api"):
            self.proxy_request()
        else:
            super().do_POST()

    def do_PUT(self):
        if self.path.startswith("/api"):
            self.proxy_request()
        else:
            self.send_response(405)
            self.end_headers()

    def do_DELETE(self):
        if self.path.startswith("/api"):
            self.proxy_request()
        else:
            self.send_response(405)
            self.end_headers()

    def proxy_request(self):
        target_url = f"{BACKEND_URL}{self.path}"
        content_length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(content_length) if content_length > 0 else None

        req_headers = {}
        for key in ("Content-Type", "Authorization", "X-User-Id", "Accept"):
            val = self.headers.get(key)
            if val:
                req_headers[key] = val

        req = urllib.request.Request(target_url, data=body, headers=req_headers, method=self.command)
        try:
            with urllib.request.urlopen(req) as resp:
                self.send_response(resp.status)
                for header, value in resp.getheaders():
                    if header.lower() not in ("transfer-encoding", "content-length", "access-control-allow-origin"):
                        self.send_header(header, value)
                resp_body = resp.read()
                self.send_header("Content-Length", str(len(resp_body)))
                self.end_headers()
                self.wfile.write(resp_body)
        except urllib.error.HTTPError as e:
            self.send_response(e.code)
            for header, value in e.headers.items():
                if header.lower() not in ("transfer-encoding", "content-length", "access-control-allow-origin"):
                    self.send_header(header, value)
            err_body = e.read()
            self.send_header("Content-Length", str(len(err_body)))
            self.end_headers()
            self.wfile.write(err_body)
        except Exception as ex:
            self.send_response(502)
            self.end_headers()
            self.wfile.write(f"Proxy Error: {str(ex)}".encode())

    def log_message(self, format, *args):
        # Clean log message
        sys.stderr.write(f"[DevServer] {args[0]} -> {args[1]}\n")

if __name__ == "__main__":
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), LiveDevelopmentHandler) as httpd:
        print(f"==================================================")
        print(f" Live Dev Server listening on http://localhost:{PORT}")
        print(f" Serving live directory: {FRONT_END_DIR}")
        print(f" Reverse proxying /api/* -> {BACKEND_URL}")
        print(f" (Browser cache disabled: Instant live reload on refresh)")
        print(f"==================================================")
        httpd.serve_forever()
