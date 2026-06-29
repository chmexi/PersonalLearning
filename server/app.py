"""道痕服务器"""
import sys, os
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_sqlalchemy import SQLAlchemy
from datetime import date, datetime

db = SQLAlchemy()

# ===== 模型 =====
class DaoHenEntry(db.Model):
    __tablename__ = "daohen_entries"
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    date = db.Column(db.String(10), nullable=False, index=True, unique=True)  # "2026-06-29"
    q1 = db.Column(db.Text, default="")   # 最起波澜的一件事
    q2 = db.Column(db.Text, default="")   # 第一反应
    q3 = db.Column(db.Text, default="")   # 其实想得到什么
    q4 = db.Column(db.Text, default="")   # 其实在害怕什么
    q5 = db.Column(db.Text, default="")   # 给自己找了什么理由
    q6 = db.Column(db.Text, default="")   # 主石头
    q7 = db.Column(db.Text, default="")   # 明天怎么做
    created_at = db.Column(db.DateTime, default=lambda: datetime.utcnow())
    updated_at = db.Column(db.DateTime, default=lambda: datetime.utcnow(), onupdate=lambda: datetime.utcnow())

# ===== 路由 =====
app = Flask(__name__)
app.config["SQLALCHEMY_DATABASE_URI"] = os.environ.get(
    "DATABASE_URL", f"sqlite:///{os.path.join(os.path.dirname(__file__), 'daohen.db')}"
)
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
CORS(app)
db.init_app(app)

with app.app_context():
    db.create_all()

@app.route("/api/daohen", methods=["GET"])
def get_entry():
    date_str = request.args.get("date", date.today().isoformat())
    entry = DaoHenEntry.query.filter_by(date=date_str).first()
    if not entry:
        return jsonify(None), 200
    return jsonify({
        "id": entry.id, "date": entry.date,
        "q1": entry.q1, "q2": entry.q2, "q3": entry.q3,
        "q4": entry.q4, "q5": entry.q5, "q6": entry.q6, "q7": entry.q7,
        "updatedAt": entry.updated_at.isoformat() if entry.updated_at else None,
    })

@app.route("/api/daohen/range", methods=["GET"])
def get_range():
    start = request.args.get("start", "")
    end = request.args.get("end", "")
    query = DaoHenEntry.query
    if start:
        query = query.filter(DaoHenEntry.date >= start)
    if end:
        query = query.filter(DaoHenEntry.date <= end)
    entries = query.order_by(DaoHenEntry.date.desc()).all()
    return jsonify([{
        "id": e.id, "date": e.date, "q6": e.q6,
    } for e in entries])

@app.route("/api/daohen/sync", methods=["POST"])
def sync_entry():
    data = request.get_json() or {}
    date_str = data.get("date", date.today().isoformat())
    entry = DaoHenEntry.query.filter_by(date=date_str).first()
    if entry:
        for f in ("q1","q2","q3","q4","q5","q6","q7"):
            if f in data:
                setattr(entry, f, data[f])
    else:
        entry = DaoHenEntry(
            date=date_str, q1=data.get("q1",""), q2=data.get("q2",""),
            q3=data.get("q3",""), q4=data.get("q4",""), q5=data.get("q5",""),
            q6=data.get("q6",""), q7=data.get("q7",""),
        )
        db.session.add(entry)
    db.session.commit()
    return jsonify({"id": entry.id, "date": entry.date}), 200

@app.route("/api/daohen/recent", methods=["GET"])
def get_recent():
    """获取最近N条记录（首页用）"""
    limit = request.args.get("limit", 5, type=int)
    entries = DaoHenEntry.query.order_by(DaoHenEntry.date.desc()).limit(limit).all()
    return jsonify([{
        "id": e.id, "date": e.date, "q1": e.q1[:40], "q6": e.q6[:40],
    } for e in entries])

@app.route("/api/daohen/yesterday", methods=["GET"])
def get_yesterday():
    """获取昨天的主石头"""
    from datetime import timedelta
    yesterday = (date.today() - timedelta(days=1)).isoformat()
    entry = DaoHenEntry.query.filter_by(date=yesterday).first()
    if not entry:
        return jsonify(None), 200
    return jsonify({"date": entry.date, "q6": entry.q6, "q1": entry.q1[:60]})

if __name__ == "__main__":
    print(" * 道痕服务器启动: http://0.0.0.0:5001")
    app.run(host="0.0.0.0", port=5001, debug=True)
