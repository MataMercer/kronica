import { fetchArticle } from "@/app/fetch/articles";
import Image from "next/image";

export default async function ArticlePage(
    props: {
        params: Promise<{ id: string }>;
    }
) {
    const params = await props.params;
    const id = params.id;
    const article = id && (await fetchArticle(id));

    return (
        <div>
            {article && (
                <div>
                    <h1 className="text-3xl font-bold">
                        {article && article.title}
                    </h1>
                    {article.timeline && (
                        <h2>Timeline {article.timeline.name}</h2>
                    )}
                    {article.attachments.map((it) => (
                        <Image
                            key={it.id}
                            width={200}
                            height={200}
                            src={`http://localhost:7070/files/serve/${it.id}/${it.name}`}
                            alt="article attachment"
                        />
                    ))}
                    <p>{article && article.body}</p>

                    <h1 className="text-2xl">Comments</h1>
                </div>
            )}
        </div>
    );
}
