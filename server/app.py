"""道痕服务器"""
import sys, os, json, time, threading, hmac
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from flask import Flask, request, jsonify, send_file, url_for
from flask_cors import CORS
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import inspect, text
from datetime import date, datetime
import requests

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
    tags = db.Column(db.Text, default="")
    action_status = db.Column(db.Integer, nullable=False, default=0)
    action_note = db.Column(db.Text, default="")
    created_at = db.Column(db.DateTime, default=lambda: datetime.utcnow())
    updated_at = db.Column(db.DateTime, default=lambda: datetime.utcnow(), onupdate=lambda: datetime.utcnow())
    revision = db.Column(db.Integer, nullable=False, default=1)
    transcript = db.Column(db.Text, default="")
    facts = db.Column(db.Text, default="")
    emotions = db.Column(db.Text, default="")
    stone = db.Column(db.Text, default="")
    better_choice = db.Column(db.Text, default="")
    ai_question = db.Column(db.Text, default="")
    analysis_source = db.Column(db.Text, default="")
    analyzed_at = db.Column(db.Text, default="")

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
    columns = {column["name"] for column in inspect(db.engine).get_columns("daohen_entries")}
    if "revision" not in columns:
        db.session.execute(
            text("ALTER TABLE daohen_entries ADD COLUMN revision INTEGER NOT NULL DEFAULT 1")
        )
        db.session.commit()
    if "tags" not in columns:
        db.session.execute(text("ALTER TABLE daohen_entries ADD COLUMN tags TEXT NOT NULL DEFAULT ''"))
    if "action_status" not in columns:
        db.session.execute(text("ALTER TABLE daohen_entries ADD COLUMN action_status INTEGER NOT NULL DEFAULT 0"))
    if "action_note" not in columns:
        db.session.execute(text("ALTER TABLE daohen_entries ADD COLUMN action_note TEXT NOT NULL DEFAULT ''"))
    for column in ("transcript", "facts", "emotions", "stone", "better_choice", "ai_question", "analysis_source", "analyzed_at"):
        if column not in columns:
            db.session.execute(text(f"ALTER TABLE daohen_entries ADD COLUMN {column} TEXT NOT NULL DEFAULT ''"))
    db.session.commit()


def entry_json(entry):
    return {
        "id": entry.id, "date": entry.date,
        "q1": entry.q1, "q2": entry.q2, "q3": entry.q3,
        "q4": entry.q4, "q5": entry.q5, "q6": entry.q6, "q7": entry.q7,
        "tags": entry.tags, "actionStatus": entry.action_status, "actionNote": entry.action_note,
        "transcript": entry.transcript or "", "facts": entry.facts or "",
        "emotions": entry.emotions or "", "stone": entry.stone or "",
        "betterChoice": entry.better_choice or "", "aiQuestion": entry.ai_question or "",
        "analysisSource": entry.analysis_source or "", "analyzedAt": entry.analyzed_at or "",
        "revision": entry.revision,
        "updatedAt": entry.updated_at.isoformat() if entry.updated_at else None,
    }

@app.route("/api/daohen", methods=["GET"])
def get_entry():
    date_str = request.args.get("date", date.today().isoformat())
    entry = DaoHenEntry.query.filter_by(date=date_str).first()
    if not entry:
        return jsonify(None), 200
    return jsonify(entry_json(entry))

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
    return jsonify([entry_json(e) for e in entries])

@app.route("/api/daohen/sync", methods=["POST"])
def sync_entry():
    data = request.get_json() or {}
    date_str = data.get("date", date.today().isoformat())
    entry = DaoHenEntry.query.filter_by(date=date_str).first()
    if entry:
        client_revision = data.get("revision", 0)
        if client_revision != entry.revision:
            return jsonify(entry_json(entry)), 409
        field_map = {
            "q1": "q1", "q2": "q2", "q3": "q3", "q4": "q4",
            "q5": "q5", "q6": "q6", "q7": "q7", "tags": "tags",
            "actionStatus": "action_status", "actionNote": "action_note",
            "transcript": "transcript", "facts": "facts", "emotions": "emotions",
            "stone": "stone", "betterChoice": "better_choice", "aiQuestion": "ai_question",
            "analysisSource": "analysis_source", "analyzedAt": "analyzed_at",
        }
        for field, attribute in field_map.items():
            if field in data:
                setattr(entry, attribute, data[field])
        entry.revision += 1
    else:
        if data.get("revision", 0) != 0:
            return jsonify({"message": "云端记录已不存在，请重新同步"}), 409
        entry = DaoHenEntry(
            date=date_str, q1=data.get("q1",""), q2=data.get("q2",""),
            q3=data.get("q3",""), q4=data.get("q4",""), q5=data.get("q5",""),
            q6=data.get("q6",""), q7=data.get("q7",""),
            tags=data.get("tags", ""), action_status=data.get("actionStatus", 0),
            action_note=data.get("actionNote", ""),
            transcript=data.get("transcript", ""), facts=data.get("facts", ""),
            emotions=data.get("emotions", ""), stone=data.get("stone", ""),
            better_choice=data.get("betterChoice", ""), ai_question=data.get("aiQuestion", ""),
            analysis_source=data.get("analysisSource", ""), analyzed_at=data.get("analyzedAt", ""),
        )
        db.session.add(entry)
    db.session.commit()
    return jsonify(entry_json(entry)), 200

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


AI_LIMIT = 10
AI_WINDOW_SECONDS = 60
_ai_requests = {}
_ai_lock = threading.Lock()


def _allow_ai_request(ip):
    now = time.monotonic()
    with _ai_lock:
        recent = [stamp for stamp in _ai_requests.get(ip, []) if now - stamp < AI_WINDOW_SECONDS]
        if len(recent) >= AI_LIMIT:
            _ai_requests[ip] = recent
            return False
        recent.append(now)
        _ai_requests[ip] = recent
        return True


def _json_from_model(content):
    content = (content or "").strip()
    if content.startswith("```"):
        lines = content.splitlines()
        lines = lines[1:] if lines and lines[0].strip().startswith("```") else lines
        lines = lines[:-1] if lines and lines[-1].strip() == "```" else lines
        content = "\n".join(lines).strip()
    value = json.loads(content)
    if not isinstance(value, dict):
        raise ValueError("model response must be an object")
    return value


def _validate_analysis(value):
    required = {"facts", "emotions", "stone", "betterChoice", "questionForUser"}
    if set(value) != required or not isinstance(value["facts"], list) or not all(isinstance(x, str) for x in value["facts"]):
        raise ValueError("invalid analysis fields")
    for item in value["emotions"]:
        if not isinstance(item, dict) or set(item) != {"name", "intensity", "evidence"}:
            raise ValueError("invalid emotion")
        if not isinstance(item["name"], str) or not isinstance(item["evidence"], str) or not isinstance(item["intensity"], int) or not 0 <= item["intensity"] <= 10:
            raise ValueError("invalid emotion type")
    stone = value["stone"]
    if not isinstance(stone, dict) or set(stone) != {"pattern", "confidence", "alternative"}:
        raise ValueError("invalid stone")
    if not isinstance(stone["pattern"], str) or not isinstance(stone["alternative"], str) or not isinstance(stone["confidence"], (int, float)) or not 0 <= stone["confidence"] <= 1:
        raise ValueError("invalid stone type")
    choice = value["betterChoice"]
    if not isinstance(choice, dict) or set(choice) != {"trigger", "action", "smallestStep"} or not all(isinstance(choice[k], str) for k in choice):
        raise ValueError("invalid betterChoice")
    if not isinstance(value["questionForUser"], str):
        raise ValueError("invalid question")
    return value


@app.route("/api/ai/daohen/analyze", methods=["POST"])
def analyze_daohen():
    configured_token = os.environ.get("DAOHEN_AI_TOKEN", "").strip()
    authorization = request.headers.get("Authorization", "")
    if not configured_token or not authorization.startswith("Bearer ") or not hmac.compare_digest(authorization[7:].strip(), configured_token):
        return jsonify({"message": "未授权"}), 401
    if not _allow_ai_request(request.remote_addr or "unknown"):
        return jsonify({"message": "请求过于频繁，请稍后再试"}), 429
    payload = request.get_json(silent=True) or {}
    if not isinstance(payload, dict):
        return jsonify({"message": "请求内容格式无效"}), 400
    transcript = payload.get("transcript", "")
    if not isinstance(transcript, str) or not transcript.strip():
        return jsonify({"message": "讲述文字不能为空"}), 400
    transcript = transcript.strip()
    if len(transcript) > 12000:
        return jsonify({"message": "讲述文字不能超过 12000 字"}), 400
    api_key = os.environ.get("DEEPSEEK_API_KEY", "").strip()
    if not api_key:
        return jsonify({"message": "服务端尚未配置 DeepSeek API Key"}), 503
    base_url = os.environ.get("DEEPSEEK_BASE_URL", "https://api.deepseek.com").rstrip("/")
    model = os.environ.get("DEEPSEEK_MODEL", "deepseek-chat")
    system_prompt = (
        "你是一个谨慎的自我反思助手。不要诊断心理疾病，不要虚构用户没有提到的事实。"
        "石头只表示反复出现的恐惧、渴望、防御或行为模式。证据不足时降低 confidence 并填写 alternative。"
        "只返回约定字段的 JSON，不要 Markdown。字段必须是 facts 字符串数组、emotions 对象数组、"
        "stone 对象、betterChoice 对象和 questionForUser 字符串。"
    )
    try:
        response = requests.post(
            f"{base_url}/chat/completions",
            headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
            json={"model": model, "temperature": 0.2, "response_format": {"type": "json_object"}, "messages": [
                {"role": "system", "content": system_prompt}, {"role": "user", "content": transcript}
            ]},
            timeout=60,
        )
        response.raise_for_status()
        content = response.json()["choices"][0]["message"]["content"]
        return jsonify(_validate_analysis(_json_from_model(content))), 200
    except (requests.RequestException, ValueError, KeyError, TypeError, IndexError, json.JSONDecodeError):
        return jsonify({"message": "DeepSeek 返回结果格式无效或暂时不可用"}), 502


@app.route("/api/app/update", methods=["GET"])
def get_app_update():
    """Return the latest Android release metadata."""
    apk_path = os.environ.get(
        "APP_APK_PATH",
        os.path.join(os.path.dirname(__file__), "releases", "app-release.apk"),
    )
    apk_exists = os.path.isfile(apk_path)
    configured_url = os.environ.get("APP_APK_URL", "").strip()
    apk_url = configured_url or (
        url_for("download_app_update", _external=True) if apk_exists else ""
    )
    return jsonify({
        "versionCode": int(os.environ.get("APP_VERSION_CODE", "3")),
        "versionName": os.environ.get("APP_VERSION_NAME", "1.2"),
        "apkUrl": apk_url,
        "releaseNotes": os.environ.get(
            "APP_RELEASE_NOTES",
            "新增行动兑现、主题标签、本周复盘和历史搜索",
        ),
        "publishedAt": os.environ.get("APP_PUBLISHED_AT", ""),
        "fileSize": os.path.getsize(apk_path) if apk_exists else 0,
    })


@app.route("/api/app/download", methods=["GET"])
def download_app_update():
    """Download the configured Android release APK."""
    apk_path = os.environ.get(
        "APP_APK_PATH",
        os.path.join(os.path.dirname(__file__), "releases", "app-release.apk"),
    )
    if not os.path.isfile(apk_path):
        return jsonify({"message": "APK not published"}), 404
    return send_file(
        apk_path,
        mimetype="application/vnd.android.package-archive",
        as_attachment=True,
        download_name="personal-learning.apk",
    )

if __name__ == "__main__":
    print(" * 道痕服务器启动: http://0.0.0.0:5001")
    app.run(host="0.0.0.0", port=5001, debug=os.environ.get("FLASK_DEBUG") == "1")
