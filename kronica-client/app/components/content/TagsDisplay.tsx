import { Tag } from "@/app/Types/Models";
import TagComp from "@/components/CustomUi/Tag";

export default function TagsDisplay({ tags }: { tags: Tag[] }) {
    return (
        <div>
            {tags && (
                <div className="flex">
                    {tags.map((tag) => (
                        <TagComp key={tag.id}>{tag.name}</TagComp>
                    ))}
                </div>
            )}
        </div>
    );
}
