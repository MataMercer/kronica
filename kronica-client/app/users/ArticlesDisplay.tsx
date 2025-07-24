import ArticleThumb from "@/app/components/articles/ArticleThumb";
import {fetchAllArticles} from "@/app/fetch/articles";
import {fetchTimeline} from "../fetch/timelines";
import {fetchCharacters} from "@/app/fetch/characters";
import Link from "next/link";
import Image from "next/image";
import Pagination from "@/components/CustomUi/Pagination";
import EmptyPlaceholder from "@/components/CustomUi/EmptyPlaceholder";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu";
import {EllipsisVertical, Flag, PencilIcon, Settings, Trash} from "lucide-react";
import AuthProtection from "@/app/auth/AuthProtection";

type ArticleDisplayProps = {
    timelineId: number;
    authorId: number;
    page?: number;
};
export default async function ArticleDisplay({
                                                 timelineId,
                                                 authorId,
                                                 page = 1,
                                             }: ArticleDisplayProps) {
    const articlePage = await fetchAllArticles(authorId, timelineId, page);
    const {content: articles, pages: articlePages} = articlePage || {
        content: [],
    };
    const timeline = timelineId
        ? await fetchTimeline(timelineId.toString())
        : undefined;

    const characters = (await fetchCharacters(authorId, timelineId))?.content;
    return (
        <div className="grow">
            {timeline && (
                <>
                    <h1 className="m-2 p-2 text-center font-extrabold text-4xl border-black border-b-[1px] mb-5">
                        {timeline.name}
                    </h1>
                    <p>{timeline.description}</p>
                </>
            )}
            <h1 className="text-3xl border-black border-b-[1px] mb-5">
                Characters
            </h1>
            {characters && characters.length > 0 ? (
                <div>
                    <div className="grid sm:grid-cols-5 gap-2">
                        {characters.map((c) => (
                            <Link
                                className="flex flex-col items-center font-bold border-black border-[1px] border-b-[5px] mt-1 mb-1 p-1"
                                key={c.id}
                                href={`/characters/${c.id}`}
                            >
                                {c.profilePictures.length > 0 ? (
                                    <Image
                                        className="rounded-full object-fill"
                                        src={`http://localhost:7070/api/files/serve/${c.profilePictures[0]?.storageId}/${c.profilePictures[0]?.name}`}
                                        alt="Character Profile Picture"
                                        width="50"
                                        height="50"
                                    />
                                ) : (
                                    <Image
                                        className="rounded-full object-fill"
                                        alt="website logo"
                                        src="/placeholder.jpg"
                                        width={100}
                                        height={100}
                                    />
                                )}
                                <div className={"flex justify-between space-x-1 sm:space-y-0"}>
                                    <div>{c.name}</div>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger className="p-0 ">
                                            <EllipsisVertical size={20}/>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent>
                                            <AuthProtection
                                                requiredRole={"AUTHENTICATED_USER"}
                                                requiredOwnerId={c.author.id}
                                            >
                                                <DropdownMenuItem>
                                                    <Link
                                                        href={`/characters/${c.id}/edit`}
                                                        className="flex items-center space-x-1"
                                                    >
                                                        <PencilIcon/>
                                                        <span>EDIT</span>
                                                    </Link>
                                                </DropdownMenuItem>
                                                <DropdownMenuItem
                                                    // onClick={() => onDelete(timeline.id)}
                                                >
                                                    <Trash/>
                                                    <span>DELETE</span>
                                                </DropdownMenuItem>
                                            </AuthProtection>

                                            <DropdownMenuItem>
                                                <Flag/>
                                                <span>REPORT</span>
                                            </DropdownMenuItem>
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </div>
                            </Link>
                        ))}
                    </div>
                </div>
            ) : (
                <EmptyPlaceholder/>
            )}
            <div className="flex space-x-2 border-black border-b-[1px] mb-1 justify-between">
                <h1 className="text-3xl ">Articles</h1>
            </div>

            {articles && articles.length > 0 ? (
                <div className="grid sm:grid-cols-5 gap-2">
                    {
                        articles.map((article) => (
                            <ArticleThumb key={article.id} article={article}/>
                        ))
                    }
                </div>
            ) : (
                <EmptyPlaceholder/>
            )}
            {articlePages && articles.length > 0 && (
                <Pagination currentPage={page} totalPages={articlePages}/>
            )}
        </div>
    );
}
