import { PenTool, PersonStanding, Rows4 } from "lucide-react";
import Link from "next/link";
import { fetchAllArticles } from "../fetch/articles";
import AuthProtectionSSR from "../auth/AuthProtectionSSR";
import ArticleThumb from "../components/articles/ArticleThumb";
import { fetchCurrentUser } from "../fetch/auth";
import { redirect } from "next/navigation";

export default async function Home() {
    const articles = (await fetchAllArticles())?.content;

    const currentUser = await fetchCurrentUser();

    if (!currentUser.id) {
        redirect("/home");
    }

    return (
        <>
            <h1 className="text-3xl self-center ">HOME</h1>
            <AuthProtectionSSR requiredRole="CONTRIBUTOR_USER">
                <div className="self-center flex">
                    <div className="m-2 ">
                        <Link
                            href="/articles/create"
                            className="flex content-center"
                        >
                            <button className="button flex space-x-1">
                                <PenTool />
                                <span>CREATE ARTICLE</span>
                            </button>
                        </Link>
                    </div>
                    <div className="m-2 ">
                        <Link
                            href="/timelines/create"
                            className="flex content-center"
                        >
                            <button className="button flex space-x-1">
                                <Rows4 />
                                <span>CREATE TIMELINE</span>
                            </button>
                        </Link>
                    </div>
                    <div className="m-2 ">
                        <Link
                            href="/characters/create"
                            className="flex content-center"
                        >
                            <button className="button flex space-x-1">
                                <PersonStanding />
                                <span>CREATE CHARACTER</span>
                            </button>
                        </Link>
                    </div>
                </div>
            </AuthProtectionSSR>

            <div className="space-y-4">
                <h2 className="text-2xl border-black border-b-[1px]">
                    NEW FROM EVERYONE
                </h2>
                <div className="grid sm:grid-cols-5 gap-2">
                    {articles ? (
                        articles.map((article) => (
                            <ArticleThumb key={article.id} article={article} />
                        ))
                    ) : (
                        <div>There are no articles.</div>
                    )}
                </div>
                <h2 className="text-2xl border-black border-b-[1px] mb-10">
                    NEW FROM FOLLOWING
                </h2>
                <div className="grid sm:grid-cols-5 gap-2">
                    {articles ? (
                        articles.map((article) => (
                            <ArticleThumb key={article.id} article={article} />
                        ))
                    ) : (
                        <div>There are no articles.</div>
                    )}
                </div>
            </div>
        </>
    );
}
