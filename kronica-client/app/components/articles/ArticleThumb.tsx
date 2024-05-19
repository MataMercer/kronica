"use client";

import { refreshArticles } from "@/app/actions";
import { Article } from "@/app/fetch/articles";
import MDEditor from "@uiw/react-md-editor";

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
            className="flex flex-row justify-between border-black border-[1px] border-b-[5px] mt-3 mb-3 p-3"
            key={article.id}
        >
            <div className="flex flex-col">
                <h3 className="text-xl font-bold capitalize">
                    {article.title}
                </h3>
                <div>BY: {article.author.name}</div>
                <div data-color-mode="light">
                    {article.body ? (
                        <MDEditor.Markdown source={article.body} />
                    ) : (
                        "[Empty article.]"
                    )}
                </div>
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
    );
}
