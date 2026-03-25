#!/usr/bin/env python3
import sys
import os
import json
import base64
import tempfile

def decode_base64_image(base64_image):
    if ',' in base64_image:
        base64_image = base64_image.split(',')[1]
    try:
        return base64.b64decode(base64_image)
    except Exception as e:
        raise ValueError(f"Invalid base64 image data: {e}")


def image_to_embedding(base64_image):
    try:
        from deepface import DeepFace
    except ImportError:
        raise ImportError("deepface module is required. install with pip install deepface")

    binary = decode_base64_image(base64_image)

    with tempfile.NamedTemporaryFile(suffix='.jpg', delete=False) as tmp:
        tmp.write(binary)
        temp_path = tmp.name

    try:
        # Use VGG-Face for compatibility with existing pipeline
        results = DeepFace.represent(img_path=temp_path, model_name='VGG-Face', enforce_detection=False)
        if not results or len(results) == 0 or 'embedding' not in results[0]:
            raise ValueError('Could not generate embedding from image')
        return results[0]['embedding']
    finally:
        try:
            os.remove(temp_path)
        except Exception:
            pass


def cosine_distance(a, b):
    import numpy as np
    a = np.asarray(a, dtype=np.float64)
    b = np.asarray(b, dtype=np.float64)
    if a.size == 0 or b.size == 0:
        return 1.0
    dot = np.dot(a, b)
    norm_a = np.linalg.norm(a)
    norm_b = np.linalg.norm(b)
    if norm_a == 0 or norm_b == 0:
        return 1.0
    similarity = dot / (norm_a * norm_b)
    return 1.0 - similarity


def main():
    try:
        payload = json.load(sys.stdin)

        image = payload.get('image')
        if not image:
            print(json.dumps({'match': False, 'message': 'No image provided'}))
            return

        threshold = float(payload.get('threshold', 0.4))
        candidates = payload.get('candidates', [])

        if not candidates:
            print(json.dumps({'match': False, 'message': 'No registered faces'}))
            return

        query_emb = image_to_embedding(image)

        best_uid = None
        best_name = None
        best_dist = float('inf')

        for candidate in candidates:
            uid = candidate.get('uid')
            face_data = candidate.get('faceData')
            if not face_data or not uid:
                continue

            candidate_emb = None
            # Check if face_data is a JSON array (legacy or pre-calculated embedding)
            if isinstance(face_data, str) and face_data.strip().startswith('['):
                try:
                    candidate_emb = json.loads(face_data)
                except:
                    pass
            
            if candidate_emb is None:
                # Treat as base64 image
                try:
                    candidate_emb = image_to_embedding(face_data)
                except Exception as e:
                    continue

            dist = cosine_distance(query_emb, candidate_emb)
            if dist < best_dist:
                best_dist = dist
                best_uid = uid
                best_name = candidate.get('name', uid)

        if best_uid is not None and best_dist <= threshold:
            print(json.dumps({'match': True, 'uid': best_uid, 'name': best_name, 'distance': float(best_dist)}))
        else:
            message = f"No match found (best dist: {best_dist:.4f})" if best_dist < 1.0 else "No face detected"
            print(json.dumps({'match': False, 'message': message, 'distance': float(best_dist)}))

    except json.JSONDecodeError:
        print(json.dumps({'match': False, 'message': 'Invalid JSON payload'}))
    except ImportError as e:
        print(json.dumps({'match': False, 'message': f'Dependency error: {str(e)}'}))
    except Exception as e:
        print(json.dumps({'match': False, 'message': f'Internal error: {str(e)}'}))


if __name__ == '__main__':
    main()
