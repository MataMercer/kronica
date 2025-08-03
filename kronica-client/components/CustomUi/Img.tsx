import { ImagePresetSize } from "@/app/Types/ImagePresetSize";
import Image, { ImageProps } from "next/image";
import { useMemo } from "react";

export default function Img(
    props: Omit<ImageProps, "src"> & {
        storageId: string;
        size: ImagePresetSize;
    }
) {
    const { storageId, size } = props;
    const queryParams = useMemo(() => {
        const searchParams = new URLSearchParams();
        searchParams.set("image_size", size);
        return searchParams;
    }, [size]);

    return (
        <Image
            {...props}
            src={`http://localhost:7070/api/files/serve/${storageId}?${queryParams}`}
            alt={"DERP DERP HERRRRRRRR"}
        />
    );
}
