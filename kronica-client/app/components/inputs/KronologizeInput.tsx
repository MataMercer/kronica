import { MouseEvent, useState, useCallback, useEffect } from "react";
import { useDropzone } from "react-dropzone";
import { DndContext, UniqueIdentifier } from "@dnd-kit/core";
import SortableInput from "./SortableInput";

export type OrderInput = {
    id: string;
    title: string;
};

type KronologizeInputProps = {
    id: string;
    order: OrderInput[];
    setOrder: (arg0: OrderInput[]) => void;
};

function ArticleThumb({ id, title }: OrderInput) {
    return <div className="p-2 flex flex-col">{title}</div>;
}

function KronologizeInput({ id, order, setOrder }: KronologizeInputProps) {
    // const [fileInputsState, setFileInputsState] = useState<FileInput[]>([]);

    // useEffect(() => {
    //   setFileInputsState(fileInputs);
    // }, [fileInputs]);

    const [increm, setIncrem] = useState(0);

    return (
        <>
            <SortableInput<OrderInput>
                list={order}
                setList={setOrder}
                cardComponent={ArticleThumb}
            />
        </>
    );
}

export default KronologizeInput;
