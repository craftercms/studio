def session = request.getSession(false);
if (session != null) {
    session.invalidate();
}
