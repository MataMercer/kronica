import { fetchArticle } from "@/app/fetch/articles";
import { ImagePresetSize } from "@/app/Types/ImagePresetSize";
import Img from "@/components/CustomUi/Img";
import Image from "next/image";
import Link from "next/link";

export default async function ArticlePage(props: {
    params: Promise<{ id: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    const article = id && (await fetchArticle(id));

    return (
        <div>
            {article && (
                <div className="space-y-4">
                    <h1 className="text-3xl font-bold">
                        {article && article.title}
                    </h1>
                    Timeline:
                    {article.timeline && (
                        <Link
                            href={`/users/${article.author.id}/${article.timeline.id}`}
                            className=""
                        >
                            <h2 className="text-xl">{article.timeline.name}</h2>
                        </Link>
                    )}
                    <p>{article && article.body}</p>
                    <h2 className="subheading">Gallery</h2>
                    <div className="grid sm:grid-cols-5 gap-2">
                        {article.attachments.map((it, index) => (
                            <Link
                                href={`/articles/${article.id}/page/${
                                    index + 1
                                }`}
                                className="flex flex-col justify-center items-center border-black border-[1px] border-b-[5px]"
                                key={it.id}
                            >
                                <Img
                                    width={200}
                                    height={200}
                                    alt="article attachment"
                                    storageId={it.storageId}
                                    size="MEDIUM"
                                />
                                <div>Page {index + 1}</div>
                            </Link>
                        ))}
                    </div>
                    <h1 className="text-2xl subheading">Comments</h1>
                </div>
            )}
        </div>
    );
}
