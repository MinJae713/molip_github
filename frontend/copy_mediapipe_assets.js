import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const projectRoot = __dirname;
const nodeModules = path.join(projectRoot, 'node_modules');
const publicDir = path.join(projectRoot, 'public');
const destBase = path.join(publicDir, 'mediapipe');

const copyFiles = (srcDir, destDir, files) => {
    if (!fs.existsSync(destDir)) {
        fs.mkdirSync(destDir, { recursive: true });
        console.log(`Created directory: ${destDir}`);
    }

    files.forEach(file => {
        const srcPath = path.join(srcDir, file);
        const destPath = path.join(destDir, file);
        if (fs.existsSync(srcPath)) {
            fs.copyFileSync(srcPath, destPath);
            console.log(`Copied ${file} to ${destDir}`);
        } else {
            console.error(`File not found: ${srcPath}`);
        }
    });
};

const faceMeshSrc = path.join(nodeModules, '@mediapipe', 'face_mesh');
const faceMeshDest = path.join(destBase, 'face_mesh');
const faceMeshFiles = [
    'face_mesh_solution_packed_assets.data',
    'face_mesh_solution_packed_assets_loader.js',
    'face_mesh_solution_simd_wasm_bin.wasm',
    'face_mesh_solution_wasm_bin.wasm',
    'face_mesh_solution_simd_wasm_bin.js',
    'face_mesh_solution_wasm_bin.js',
    'face_mesh.binarypb', // Added missing binarypb
];

const poseSrc = path.join(nodeModules, '@mediapipe', 'pose');
const poseDest = path.join(destBase, 'pose');
const poseFiles = [
    'pose_solution_packed_assets.data',
    'pose_solution_packed_assets_loader.js',
    'pose_solution_simd_wasm_bin.wasm',
    'pose_solution_wasm_bin.wasm',
    'pose_solution_simd_wasm_bin.js',
    'pose_solution_wasm_bin.js',
    'pose_web.binarypb', // Added missing binarypb
    'pose_landmark_full.tflite',
    'pose_landmark_heavy.tflite',
    'pose_landmark_lite.tflite'
];


console.log('Starting MediaPipe asset copy...');
copyFiles(faceMeshSrc, faceMeshDest, faceMeshFiles);
copyFiles(poseSrc, poseDest, poseFiles);
console.log('Copy completed.');
