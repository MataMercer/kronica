import { fetchAllArticles } from "./fetch/articles";
import ArticleThumb from "./components/articles/ArticleThumb";
import AuthProtectionSSR from "./auth/AuthProtectionSSR";
import { PenTool, PersonStanding, Rows4 } from "lucide-react";
import Link from "next/link";

export default async function Welcome() {
    const articles = (await fetchAllArticles())?.content;

    return (
        <div>
            <h1>Welcome</h1>
            <div className="welcome-container" style={{ marginTop: 32 }}>
                <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-700 px-4 py-10">
                    <div className="w-full max-w-2xl bg-white/10 backdrop-blur-lg rounded-2xl shadow-2xl p-10 flex flex-col items-center">
                        <p className="text-xl font-medium bg-gradient-to-r from-indigo-500 to-cyan-400 text-white px-6 py-4 rounded-xl shadow-lg mb-6 inline-block tracking-wide">
                            Welcome to{" "}
                            <strong className="text-black">Kronica</strong>!
                            This is your personal knowledge journal.
                        </p>
                        <ul className="space-y-4 text-lg text-white/90 mb-8">
                            <li className="flex items-center gap-2">
                                <PenTool size={20} className="text-cyan-300" />
                                Write and organize your own articles.
                            </li>
                            <li className="flex items-center gap-2">
                                <Rows4 size={20} className="text-indigo-300" />
                                Browse and revisit your knowledge.
                            </li>
                            <li className="flex items-center gap-2">
                                <PersonStanding
                                    size={20}
                                    className="text-yellow-300"
                                />
                                Manage your personal profile.
                            </li>
                        </ul>
                        <div className="mt-4">
                            <strong>
                                <Link
                                    href="/login"
                                    className="inline-block bg-yellow-400 hover:bg-yellow-300 text-slate-900 font-semibold px-6 py-2 rounded-lg shadow transition"
                                >
                                    Log in
                                </Link>{" "}
                                <span className="text-white/80">
                                    to get started!
                                </span>
                            </strong>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
