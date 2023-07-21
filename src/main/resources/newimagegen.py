import argparse
import hashlib
import json
import sys

import cv2
import numpy as np

def create_patterns(m, n, size, border, id):
    patterns = np.zeros(size, dtype=np.int32)
    if border:
        patterns[:int(size[0] / 2), :int(size[1] / 2)].fill(255)
        patterns[-1 * int(size[0] / 2):, -1 * int(size[1] / 2):].fill(255)
    length = min(size)
    row_units = list(range(0, length - 1, int(length / m)))
    row_units = row_units + [length] if len(row_units) < m + 1 else row_units
    row_units = list(map(lambda x: int(x + (size[0] - length) / 2), row_units))
    col_units = list(range(0, length - 1, int(length / n)))
    col_units = col_units + [length] if len(col_units) < n + 1 else col_units
    col_units = list(map(lambda x: int(x + (size[1] - length) / 2), col_units))
    if int((m * n - 4) / 8) > 0:
        blk = hashlib.blake2s(digest_size=int((m * n - 4) / 8))
        if (m * n - 4) % 8 != 0:
            blk.update(str(int(id) % (2 ** 8)).encode('utf-8'))
            values = blk.hexdigest()
            values = list(bin(int(values, 16))[2:].zfill(8 * blk.digest_size))
            to_fill = bin(int(int(id) / (2 ** 8)))[2:].zfill((m * n - 4) % 8)
            for b_idx, b in enumerate(to_fill[::-1]):
                values.insert(-1 * (b_idx + 1), b)
            code = list(map(int, values))
        else:
            blk.update(str(id).encode('utf-8'))
            values = blk.hexdigest()
            code = list(map(int, str(bin(int(values, 16))[2:]).zfill(m * n - 4)))
    else:
        values = []
        to_fill = bin(int(int(id) % (2 ** 8)))[2:].zfill((m * n - 4) % 8)
        for b_idx in range((m * n - 4) % 8):
            values.insert(-1 * (b_idx + 1), to_fill[::-1][b_idx])
        code = list(map(int, values))
    code.insert(0, 0)
    code.insert(n - 1, 1)
    code.insert(min(-1, -1 * (n - 2)), 1)
    code.append(0)
    for r_idx in range(len(row_units) - 1):
        for c_idx in range(len(col_units) - 1):
            patterns[row_units[r_idx]:row_units[r_idx + 1],
            col_units[c_idx]:col_units[c_idx + 1]].fill(
                code[n * r_idx + c_idx] * 255)
    if border:
        patterns_to_change = cv2.resize(
            patterns[row_units[0]:row_units[-1],
            col_units[0]:col_units[-1]].astype('float32'), (length, length))
        patterns[row_units[0]:row_units[0] + length, col_units[0]:col_units[0] + length] = patterns_to_change
    else:
        patterns = cv2.resize(
            patterns[row_units[0]:row_units[-1],
            col_units[0]:col_units[-1]].astype('float32'), (height, width))
    return patterns, values


def image_generator(m, n, size, border, id_list):
    img_list = {}
    for id in id_list:
        patterns, code = create_patterns(m, n, size, border, id)
        img_list[str(id)] = {}
        img_list[id]['code'] = code
        img_list[id]['patterns'] = patterns.tolist()
    return img_list


def save_img(img_list, file_path):
    for key, img in img_list.items():
        cv2.imwrite(file_path + str(key) + '_esl.jpg', np.asarray(img['patterns']))


if __name__ == '__main__':
    # Argument 설정
    parser = argparse.ArgumentParser()
    parser.add_argument('--pattern_size', nargs='+', type=int)
    parser.add_argument('--bezel_size', nargs='+', type=int)
    parser.add_argument('--border', type=int)
    parser.add_argument('--id_path', type=str)
    parser.add_argument('--file_path', type=str)
    args = parser.parse_args()

    # Argument 를 새로운 변수에 할당
    num_rows, num_cols = args.pattern_size
    height, width = args.bezel_size
    border_yn = args.border
    esl_id_list = [x.rstrip('\n') for x in open(args.id_path, "r").readlines()]
    file_path = args.file_path
    results = image_generator(num_rows, num_cols, (height, width), bool(border_yn), esl_id_list)
    save_img(results, file_path)
    with open(file_path + 'esl_info.json', 'w', encoding='utf-8') as file:
        json.dump([results], file)
    sys.exit()
