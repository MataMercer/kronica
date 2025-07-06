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

            </div>
        </div>
    );
}
