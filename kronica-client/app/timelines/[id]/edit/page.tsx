import TimelineForm from "@/app/forms/TimelineForm";
import Link from "next/link";

export default async function EditTimelinePage(props: {
    params: Promise<{ id: string; page: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    return (
        <div className="self-center">
            <h1 className="text-2xl self-center">Edit Timeline</h1>

                <Link href={`/timelines/${id}/reorder`}>
                    <button className="button">

                        Reorder Timeline
                    </button>
                </Link>
            <TimelineForm id={Number(id)} />
        </div>
    );
}
