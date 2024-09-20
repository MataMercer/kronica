"use client";

import { refreshArticles } from "@/app/actions";
import { Article } from "@/app/fetch/articles";
import MDEditor from "@uiw/react-md-editor";
import Image from "next/image";
import Link from "next/link";

const onDelete = async (id: number) => {
    const response = await fetch(`http://localhost:7070/api/articles/${id}`, {
        method: "DELETE",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
    });

    refreshArticles();
};

type Props = {
    article: Article;
};
export default function ArticleThumb({ article }: Props) {
    return (
        <div
            className="flex flex-row justify-center border-black border-[1px] border-b-[5px] mt-3 mb-3 p-3"
            key={article.id}
        >
            <div className="flex flex-col">
                <h3 className="text-xl font-bold capitalize">
                    <Link href={`/articles/${article.id}`}>
                        {article.title}
                    </Link>
                </h3>
                {article.attachments.length > 0 && (
                    <Image
                        key={article.attachments[0].id}
                        width={200}
                        height={200}
                        src={`http://localhost:7070/files/serve/${article.attachments[0].id}/${article.attachments[0].name}`}
                        alt="article attachment"
                        className="m-2"
                    />
                )}

                <div className="flex flex-row justify-between">
                    <div className="flex-col">
                        <div>{article.attachments.length} Images</div>
                        <div>BY: {article.author.name}</div>
                    </div>
                    <button
                        className="button"
                        onClick={() => {
                            onDelete(article.id);
                        }}
                    >
                        x
                    </button>
                </div>
            </div>
        </div>
    );
}
