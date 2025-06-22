"use client";

import { refreshArticles } from "@/app/actions";
import AuthProtection from "@/app/auth/AuthProtection";
import { Article } from "@/app/fetch/articles";
import { UserRole } from "@/app/Types/Models";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import MDEditor from "@uiw/react-md-editor";
import { EllipsisVertical, Flag, Heart, PencilIcon, Trash } from "lucide-react";
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
            className="flex flex-row justify-between border-black border-[1px] border-b-[5px] mt-1 mb-1 p-1"
            key={article.id}
        >
            <div className="flex flex-col justify-between">
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
                </div>
                {article.characters.length > 0 && (
                    <div>
                        Starring:{" "}
                        {article.characters.map((c) => (
                            <Link key={c.id} href={`/characters/${c.id}`}>
                                {c.name}
                            </Link>
                        ))}
                    </div>
                )}
                <AuthProtection requiredRole={"AUTHENTICATED_USER"}>
                    <div className="flex justify-between space-x-1">
                        <div className="flex space-x-1">
                            <button className="text-purple-500 hover:text-red-700">
                                <Heart
                                    fill={article.youLiked ? "red" : "white"}
                                />
                            </button>
                            <div>{article.likeCount} Likes</div>
                        </div>

                        <DropdownMenu>
                            <DropdownMenuTrigger className="p-1">
                                <EllipsisVertical />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent>
                                <DropdownMenuItem>
                                    <PencilIcon />
                                    EDIT
                                </DropdownMenuItem>
                                <DropdownMenuItem>
                                    <Trash />
                                    DELETE
                                </DropdownMenuItem>
                                <DropdownMenuItem>
                                    <Flag />
                                    REPORT
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </AuthProtection>
            </div>
        </div>
    );
}
