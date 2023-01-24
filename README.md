# Rebound for cookie

Trying a rebound for cookie approach to not have a samesite cookie set to default, just because of oauth

```plantuml
Browser -> Server : Initial request
Server -> Browser : Cookie: samesite=strict
Browser -> AS: Authorization request
AS -> Browser: Redirect to Callback
Browser -> Server: Callback request (no cookies)
Server -> Browser: Redirect to callback, in html/JS
Browser -> Server: Callback request (cookies)
Server -> Browser: Authenticated, redirect to home
Browser -> Server: Home request (cookies)
```

## Setup

Use `myapp.com` and `helpers.rfc` domains on /etc/host for 127.0.0.1 to have cookies with a domain name.