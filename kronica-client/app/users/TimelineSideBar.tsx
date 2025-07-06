import { fetchAllTimelines, Timeline } from "@/app/fetch/timelines";
import Link from "next/link";
import {EllipsisVertical, Flag, PencilIcon, Settings, Trash} from "lucide-react";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu";
import AuthProtection from "@/app/auth/AuthProtection";

type TimelineSideBarProps = {
    authorId: number;
    activeTimelineId: number;
};
export default async function TimelineSideBar({
    authorId,
    activeTimelineId,
}: TimelineSideBarProps) {
    const timelines = await fetchAllTimelines(authorId);
    return (
        <div className="flex flex-col   md:h-[100vh]">
            <div className="text-xl   m-1">Timelines</div>
            <div className="flex md:flex-col space-y-[2px]">
                <Link
                    href={`/users/${authorId}`}
                    key={0}
                    className={`p-1 border-[1px]  border-black border-b-4 
                ${!activeTimelineId && "bg-[#8a2be2] text-white font-bold"}
                `}
                >
                    ALL
                </Link>
                {timelines ? (
                    timelines.map((timeline) => (
                        <TimelineButton
                            key={timeline.id}
                            timeline={timeline}
                            authorId={authorId}
                            active={timeline.id === activeTimelineId}
                        />
                    ))
                ) : (
                    <div>There are no articles.</div>
                )}
            </div>
        </div>
    );
}

type TimelineButtonProps = {
    timeline: Timeline;
    authorId: number;
    active: boolean;
};
function TimelineButton({ timeline, authorId, active }: TimelineButtonProps) {
    return (
        <Link
            href={`/users/${authorId}/${timeline.id}`}
            key={timeline.id}
            className={`flex justify-between p-1 border-[1px] border-black border-b-4 timeline-side-button
                ${active && "bg-[#8a2be2] text-white  font-bold"}
                `}
        >
            <span>{timeline.name.toUpperCase()}</span>
            <div className="timeline-side-option-button flex items-center">
            <DropdownMenu >
                <DropdownMenuTrigger className="p-0 ">
                    <Settings size={20} />
                </DropdownMenuTrigger>
                <DropdownMenuContent>
                    <AuthProtection
                        requiredRole={"AUTHENTICATED_USER"}
                        requiredOwnerId={timeline.author.id}
                    >
                        <DropdownMenuItem>
                            <Link
                                href={`/timelines/${timeline.id}/edit`}
                                className="flex items-center space-x-1"
                            >
                                <PencilIcon />
                                <span>EDIT</span>
                            </Link>
                        </DropdownMenuItem>
                        <DropdownMenuItem
                            // onClick={() => onDelete(timeline.id)}
                        >
                            <Trash />
                            <span>DELETE</span>
                        </DropdownMenuItem>
                    </AuthProtection>

                    <DropdownMenuItem>
                        <Flag />
                        <span>REPORT</span>
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
            </div>
        </Link>
    );
}
