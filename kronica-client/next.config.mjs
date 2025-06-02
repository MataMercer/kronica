/** @type {import('next').NextConfig} */
import removeImports from "next-remove-imports";
const removeImportsFun = removeImports({
    options: {},
});
// const nextConfig = removeImportsFun({});

const nextConfig = {
    images: {
        remotePatterns: [
            {
                protocol: "http",
                hostname: "localhost",
                port: "7070",
                pathname: "/api/files/serve/**",
            },
        ],
    },
};
export default nextConfig;
