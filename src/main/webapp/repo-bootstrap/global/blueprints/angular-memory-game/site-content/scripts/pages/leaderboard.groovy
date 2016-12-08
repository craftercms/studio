import scripts.libs.MyDAO;

def dao = new MyDAO();
dao.init();

model.leaders = dao.getLeaders();

