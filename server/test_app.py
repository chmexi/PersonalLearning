import os
import sys
import unittest
from unittest.mock import Mock, patch

os.environ.setdefault("DATABASE_URL", "sqlite:///:memory:")
os.environ["DAOHEN_AI_TOKEN"] = "test-token"
sys.path.insert(0, os.path.dirname(__file__))

from app import app


VALID_RESPONSE = {
    "facts": ["事实"],
    "emotions": [{"name": "委屈", "intensity": 7, "evidence": "原文"}],
    "stone": {"pattern": "防御", "confidence": 0.7, "alternative": "其他解释"},
    "betterChoice": {"trigger": "再次发生", "action": "停一下", "smallestStep": "呼吸"},
    "questionForUser": "还可以怎样？",
}


class AnalyzeEndpointTest(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()
        os.environ.pop("DEEPSEEK_API_KEY", None)

    def test_unauthorized(self):
        self.assertEqual(self.client.post("/api/ai/daohen/analyze", json={"transcript": "x"}).status_code, 401)

    def test_empty_transcript(self):
        response = self.client.post(
            "/api/ai/daohen/analyze",
            headers={"Authorization": "Bearer test-token"},
            json={"transcript": " "},
        )
        self.assertEqual(response.status_code, 400)

    def test_missing_server_key(self):
        response = self.client.post(
            "/api/ai/daohen/analyze",
            headers={"Authorization": "Bearer test-token"},
            json={"transcript": "x"},
        )
        self.assertEqual(response.status_code, 503)

    @patch("app.requests.post")
    def test_valid_json_and_markdown_json(self, post):
        os.environ["DEEPSEEK_API_KEY"] = "server-only-key"
        response = Mock()
        response.raise_for_status = Mock()
        response.json.side_effect = [
            {"choices": [{"message": {"content": str(VALID_RESPONSE).replace("'", '"')}}]},
            {"choices": [{"message": {"content": "```json\n" + str(VALID_RESPONSE).replace("'", '"') + "\n```"}}]},
        ]
        post.return_value = response
        headers = {"Authorization": "Bearer test-token"}
        self.assertEqual(self.client.post("/api/ai/daohen/analyze", headers=headers, json={"transcript": "x"}).status_code, 200)
        request_payload = post.call_args.kwargs["json"]
        self.assertEqual(request_payload["response_format"], {"type": "json_object"})
        self.assertEqual(request_payload["max_tokens"], 1200)
        self.assertFalse(request_payload["stream"])
        self.assertEqual(self.client.post("/api/ai/daohen/analyze", headers=headers, json={"transcript": "y"}).status_code, 200)

    @patch("app.requests.post")
    def test_invalid_model_shape(self, post):
        os.environ["DEEPSEEK_API_KEY"] = "server-only-key"
        response = Mock()
        response.raise_for_status = Mock()
        response.json.return_value = {"choices": [{"message": {"content": "{}"}}]}
        post.return_value = response
        result = self.client.post(
            "/api/ai/daohen/analyze",
            headers={"Authorization": "Bearer test-token"},
            json={"transcript": "x"},
        )
        self.assertEqual(result.status_code, 502)

    @patch("app.requests.post")
    def test_invalid_model_types(self, post):
        os.environ["DEEPSEEK_API_KEY"] = "server-only-key"
        response = Mock()
        response.raise_for_status = Mock()
        response.json.return_value = {"choices": [{"message": {"content": (
            '{"facts":"not-an-array","emotions":[],"stone":{"pattern":"","confidence":0,"alternative":""},'
            '"betterChoice":{"trigger":"","action":"","smallestStep":""},"questionForUser":""}'
        )}}]}
        post.return_value = response
        result = self.client.post(
            "/api/ai/daohen/analyze",
            headers={"Authorization": "Bearer test-token"},
            json={"transcript": "x"},
        )
        self.assertEqual(result.status_code, 502)


if __name__ == "__main__":
    unittest.main()
