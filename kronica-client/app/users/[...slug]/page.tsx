import ArticleThumb from "@/app/components/articles/ArticleThumb";
import { fetchAllArticles, fetchArticle } from "@/app/fetch/articles";
import { fetchAllTimelines, Timeline } from "@/app/fetch/timelines";
import { fetchUser } from "@/app/fetch/users";
import Image from "next/image";
import Link from "next/link";
import TimelineSideBar from "../TimelineSideBar";
import ArticleDisplay from "../ArticlesDisplay";

export default async function UserProfilePage({
    params,
}: {
    params: { slug: string[] };
}) {
    const slug = params.slug;
    const id = (slug[0] && Number(slug[0])) as number;
    const timelineId = (slug[1] && Number(slug[1])) as number;

    return (
        <div className="flex items-stretch">
            <TimelineSideBar authorId={id} activeTimelineId={timelineId} />
            <ArticleDisplay timelineId={timelineId} />
        </div>
    );
}
