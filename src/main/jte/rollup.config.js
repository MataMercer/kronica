import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import json from "@rollup/plugin-json";

export default {
    input: 'js/Embed.js',
    output: {
        format: 'iife',
        dir: 'public/build/',
        entryFileNames: 'bundle.js',
        chunkFileNames: '[name].js',
        sourcemap: true,
    },
    plugins: [
        json(),
        resolve({ browser: true }),
        commonjs()
    ],
}
