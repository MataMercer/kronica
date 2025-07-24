import { fetchArticle } from "@/app/fetch/articles";
import {
    ArrowDown,
    ArrowLeft,
    ArrowLeftToLine,
    ArrowRight,
    ArrowRightToLine,
    SquareArrowLeft,
    SquareArrowRight,
} from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import {Article} from "@/app/Types/Models";

export default async function Page(props: {
    params: Promise<{ id: string; page: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    const page = params.page;
    const article = await fetchArticle(id);

    return (
        <div>
            {article && (
                <div className="flex flex-col items-center justify-center">
                    <Link href={`/articles/${article.id}`}>
                        <h1 className="text-3xl font-bold">{article.title}</h1>
                    </Link>

                    <Navigation article={article} page={Number(page)} />

                    <Link
                        href={`/articles/${article.id}/page/${
                            Number(page) < article.attachments.length
                                ? Number(page) + 1
                                : Number(page)
                        }`}
                    >
                        <Image
                            src={`http://localhost:7070/api/files/serve/${
                                article.attachments[Number(page) - 1]?.storageId
                            }/${article.attachments[Number(page) - 1]?.name}`}
                            alt="Article Attachment"
                            width={1200}
                            height={1200}
                        />
                    </Link>

                    <Navigation article={article} page={Number(page)} />
                    <Link href={`/articles/${article.id}`}>
                        <ArrowDown className="text-xl border-[blueviolet]  border-[2px]" />
                    </Link>
                </div>
            )}
        </div>
    );
}

type NavigationProps = {
    article: Article;
    page: number;
};

function Navigation({ article, page }: NavigationProps) {
    return (
        <div className="flex space-x-3 m-3 text-xl items-center">
            <Link
                href={`/articles/${article.id}/page/1`}
                className="border-[blueviolet]  border-[2px]"
            >
                <ArrowLeftToLine />
            </Link>
            <Link
                href={`/articles/${article.id}/page/${
                    page > 1 ? page - 1 : page
                }`}
                className="border-[blueviolet]  border-[2px]"
            >
                <ArrowLeft />
            </Link>
            <div>
                {page} / {article.attachments.length}
            </div>
            <Link
                href={`/articles/${article.id}/page/${
                    page < article.attachments.length ? page + 1 : page
                }`}
                className="border-[blueviolet]  border-[2px]"
            >
                <ArrowRight />
            </Link>
            <Link
                href={`/articles/${article.id}/page/${article.attachments.length}`}
                className="border-[blueviolet]  border-[2px]"
            >
                <ArrowRightToLine />
            </Link>
        </div>
    );
}
