import scripts.libs.MyDAO;

def dao = new MyDAO();
dao.init();

def leaders = dao.getLeaders(10);

return leaders;
